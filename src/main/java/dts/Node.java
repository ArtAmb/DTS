package dts;

import dts.commands.AppendEntriesCommand;
import dts.commands.RequestVoteCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/* IMPORTANT !!!!!!!!!!!!!!!!!!!
   RAFT INFO

https://indradhanush.github.io/blog/notes-on-raft/

*/

@Getter
@Log4j
public class Node {
    private UUID uuid;
    private NodeState state;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private Set<UUID> otherNodes = new HashSet<>();

    private int timeoutInMs;
    private int electionNumber;
    volatile private boolean disabled;
    private Timer timer = new Timer();
    private int heartbeatIntervalTimeInMs = 100;
    private final ConcurrentHashMap<UUID, Record> records;
    private LinkedList<Operation> entryLog = new LinkedList<>();


    private UUID leaderUUID;
    private UUID votedFor = null;


    public static Integer ERROR_TIMEOUT = 2000;

    @Builder
    public Node(UUID uuid,
                NodeState state,
                int timeoutInMs,
                ConcurrentHashMap<UUID, Record> records,
                boolean disabled,
                UUID leaderUUID) {
        this.uuid = uuid;
        this.state = state;
        this.timeoutInMs = timeoutInMs;
        this.electionNumber = 0;
        this.records = records;
        this.disabled = disabled;
        this.leaderUUID = leaderUUID;
    }

    public void start() {
        resetElectionTimer();
    }

    private void resetElectionTimer() {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startElection();
            }
        }, timeoutInMs);
    }

    private void startElection() {
        if (!NodeState.LEADER.equals(this.state)) {
            this.electionNumber++;
            this.state = NodeState.CANDIDATE;
            this.votedFor = uuid;
            log.info(uuid + " -> starting new election - " + electionNumber);

            startGatheringVotes();
        }

    }

    private void startGatheringVotes() {
        AllRequestSummary summary = requestVotesFromOtherNodes();

        if (summary.isSuccessesCounterAtLeastHalf()) {
            becomeLeader();
        } else {
            this.votedFor = null;
            resetElectionTimer();
        }
    }

    private AllRequestSummary requestVotesFromOtherNodes() {
        RequestVoteCommand command = RequestVoteCommand.builder().build();

        Request.RequestBuilder reqBuilder = Request.builder()
                .body(command)
                .electionNumber(electionNumber)
                .type(RequestType.REQUEST_VOTE)
                .from(uuid);

        List<Response> responses = otherNodes.parallelStream()
                .map(otherNodeUUID -> sendRequest(reqBuilder.to(otherNodeUUID).build()))
                .collect(Collectors.toList());

        return new AllRequestSummary(responses);
    }

    private void becomeLeader() {
        this.timer.cancel();
        this.state = NodeState.LEADER;
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                doLeaderJob();
            }
        }, 0, heartbeatIntervalTimeInMs);

        log.info(uuid + " became leader of election " + electionNumber);
    }

    private void doLeaderJob() {
        AllRequestSummary summary = sendAppendEntriesToAllOtherNodes();

        if (summary.isSuccessesCounterAtLeastHalf()) {
            confirmEntriesLog();
        }
    }

    private AllRequestSummary sendAppendEntriesToAllOtherNodes() {
        AppendEntriesCommand command = AppendEntriesCommand.builder().operations(Collections.emptyList()).build();

        Request.RequestBuilder reqBuilder = Request.builder()
                .body(command)
                .electionNumber(electionNumber)
                .type(RequestType.APPEND_ENTRIES)
                .from(uuid);

        List<Response> responses = otherNodes.parallelStream()
                .map(otherNodeUUID -> sendRequest(reqBuilder.to(otherNodeUUID).build()))
                .collect(Collectors.toList());

        return new AllRequestSummary(responses);
    }

    private void confirmEntriesLog() {
        // TODO ??????????
    }

    public static Node createNew() {
        return builder()
                .uuid(UUID.randomUUID())
                .state(NodeState.FOLLOWER)
                .timeoutInMs(new Random().nextInt(151) + 150)
                .records(new ConcurrentHashMap<>())
                .build();
    }

    public void fillOtherNodes(Set<UUID> allNodes) {
        this.otherNodes = allNodes.stream().filter(tmp -> !tmp.equals(uuid)).collect(Collectors.toSet());
    }

    public void appendEntries(Request request) throws InterruptedException {
        if (disabled) {
            log.info(uuid + " is DISABLED");
            Thread.sleep(ERROR_TIMEOUT);
            return;
        }

        if (request.getElectionNumber() < this.electionNumber) {
            throw new IllegalStateException(prepareErrMsgForOutdateLeader(request));
        }

        if (NodeState.LEADER.equals(state)) {
            if (request.getElectionNumber() > this.electionNumber) {
                log.info("Node " + uuid + " become follower - " + this.electionNumber + " " + request.getElectionNumber());
                this.electionNumber = request.getElectionNumber();
                this.state = NodeState.FOLLOWER;
            }
        }

        leaderUUID = request.getFrom();

        AppendEntriesCommand command = (AppendEntriesCommand) request.getBody();
        entryLog.addAll(command.getOperations());
        this.votedFor = null;
        this.electionNumber = request.getElectionNumber();
        resetElectionTimer();
    }

    private String prepareErrMsgForOutdateLeader(Request request) {
        return "You are leader of election " + request.getElectionNumber() + " but current election is " + this.electionNumber;
    }

    private String prepareErrMsgForOutdateCandidate(Request request) {
        return "You are candidate of election " + request.getElectionNumber() + " but current election is " + this.electionNumber;
    }

    public Response sendRequest(Request request) {
        try {

            Future<?> future = executor.submit(() -> Network.getInstance().handle(request));
            future.get(heartbeatIntervalTimeInMs - 10, TimeUnit.MILLISECONDS);

        } catch (Exception ex) {
            log.error(ex);
            return Response.builder().nodeId(request.getTo()).msg(ex.getMessage()).success(false).build();
        }

        return Response.builder().nodeId(request.getTo()).success(true).build();
    }

    synchronized public void disable() {
        log.info(uuid + " has been disabled");
        disabled = true;
        timer.cancel();

        log.info(uuid + " DISALED ? " + disabled);
    }

    synchronized public void enable() {
        disabled = false;
        log.info(uuid + " is working again...");
        if (this.state.equals(NodeState.LEADER)) {
            becomeLeader();
        } else {
            resetElectionTimer();
        }

        log.info(uuid + " DISALED ? " + disabled);
    }

    public void requestVote(Request request) throws InterruptedException {
        if (disabled) {
            log.info(uuid + " is DISABLED");
            Thread.sleep(ERROR_TIMEOUT);
            return;
        }

        if (votedFor != null) {
            throw new IllegalStateException(uuid + " -> already voted for node " + votedFor);
        }

        if (request.getElectionNumber() < electionNumber) {
            throw new IllegalStateException(prepareErrMsgForOutdateCandidate(request));
        }

        this.electionNumber = request.getElectionNumber();
        this.votedFor = request.getFrom();
    }
}
