package dts.core;

import dts.commands.AppendEntriesCommand;
import dts.commands.ClientUpdateCommand;
import dts.commands.RequestVoteCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lombok.val;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/* IMPORTANT !!!!!!!!!!!!!!!!!!!
   RAFT INFO

https://indradhanush.github.io/blog/notes-on-raft/
https://medium.com/@kasunindrasiri/understanding-raft-distributed-consensus-242ec1d2f521
*/

@Log4j
class EntryLog {
    private ArrayList<Operation> entryLog = new ArrayList<>();
    private int lastConfirmedOperationIdx;

    synchronized public void addAll(Collection<Operation> entries) {
        entries.forEach(this::add);
    }

    synchronized public void addAllAsLeader(Collection<Operation> operations) {
        operations.forEach(op -> {
            if (entryLog.contains(op)) {
                throw new IllegalStateException("Doubled actions - " + op.getOperationId());
            }

            int size = entryLog.size();
            op.updateIndex(size);
            entryLog.add(op.getOperationIndex(), op.markAsPotential());
        });
    }

    public void add(Operation entry) {
        if (entryLog.contains(entry)) {
            return;
        }

//        Operation prev = entryLog.get(entry.getPrevIndex());
        entryLog.add(entry.getOperationIndex(), entry.markAsPotential());
    }

    public void removeExpiredOperations(int currentTerm) {
        entryLog.removeIf(op -> op.isPotential() && !Objects.equals(op.getElectionNumber(), currentTerm));
    }

    synchronized public void removeIf(Predicate<? super Operation> filter) {
        entryLog.removeIf(filter);
    }

    public List<Operation> findOperations(int operationIndex) {
        if (entryLog.isEmpty())
            return Collections.emptyList();

        if (operationIndex < 0) {
            return entryLog.subList(0, entryLog.size());
        }

        return entryLog.subList(operationIndex, entryLog.size());
    }

    synchronized public List<Operation> confirm(int lastCommittedIdx) {
        List<Operation> confirmedOperations = entryLog.stream()
                .filter(Operation::isPotential)
                .filter(op -> op.getOperationIndex() <= lastCommittedIdx)
                .peek(Operation::confirm)
                .collect(Collectors.toList());

        if (confirmedOperations.isEmpty()) {
            return Collections.emptyList();
        }

        lastConfirmedOperationIdx = confirmedOperations.get(confirmedOperations.size() - 1).getOperationIndex();

        return confirmedOperations;
    }

    public int getLastConfirmedOperationIdx() {
        return lastConfirmedOperationIdx;
    }

    public Operation getLastConfirmedOperation() {
        if (entryLog.isEmpty()) {
            return Operation.builder().electionNumber(0).operationIndex(0).build();
        }

        return this.entryLog.get(lastConfirmedOperationIdx);
    }

    public List<Operation> confirmAll() {
        return this.confirm(Integer.MAX_VALUE);
    }

    public int getLastOperationIdx() {
        if (entryLog.isEmpty()) {
            return -1;
        }

        return this.entryLog.get(this.entryLog.size() - 1).getOperationIndex();

    }

    public List<Operation> getAll() {
        return this.entryLog;
    }
}

@Getter
class OtherNode {
    private final UUID uuid;
    private int electionNumber;
    private int lastOperationIdx;

    public OtherNode(UUID otherNodeUUID) {
        this.uuid = otherNodeUUID;
    }

    public void update(Response<AppendEntriesResult> res) {
        if (res.getBody() == null)
            return;

        this.electionNumber = res.getBody().getElectionNumber();
        this.lastOperationIdx = res.getBody().getLastIndex();
    }

    public boolean isConsistent(int electionNumber, int lastOperationIdx) {
        return Objects.equals(this.lastOperationIdx, lastOperationIdx);
//                && Objects.equals(this.electionNumber, electionNumber);
    }
}

@Getter
@Log4j
public class Node {
    private UUID uuid;
    private NodeState state;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private Map<UUID, OtherNode> otherNodes = new ConcurrentHashMap<>();

    private int timeoutInMs;
    private int electionNumber;
    private int lastOperationIndex;
    volatile private boolean disabled;
    private Timer timer = new Timer();
    private int heartbeatIntervalTimeInMs = 100;
    @Getter
    private final ConcurrentHashMap<String, Record> records;
    private EntryLog entryLog = new EntryLog();


    private UUID leaderUUID;
    private UUID votedFor = null;


    public static Integer ERROR_TIMEOUT = 2000;

    @Builder
    public Node(UUID uuid,
                NodeState state,
                int timeoutInMs,
                ConcurrentHashMap<String, Record> records,
                boolean disabled,
                UUID leaderUUID) {
        this.uuid = uuid;
        this.state = state;
        this.timeoutInMs = timeoutInMs;
        this.electionNumber = 0;
        this.lastOperationIndex = -1;
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
                .lastCommittedOperationIdx(entryLog.getLastConfirmedOperationIdx())
                .electionNumber(electionNumber)
                .type(RequestType.REQUEST_VOTE)
                .from(uuid);

        List<Response> responses = otherNodes.values().parallelStream()
                .map(OtherNode::getUuid)
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

        List<Response> responses = otherNodes.values().parallelStream()
                .map(OtherNode::getUuid)
                .map(this::appendEntriesForOneNode)
                .peek(res -> {
                    otherNodes.get(res.getNodeId()).update(res);
                })
                .collect(Collectors.toList());

        return new AllRequestSummary(responses);
    }

    private Response<AppendEntriesResult> appendEntriesForOneNode(UUID otherNodeUUID) {
        List<Operation> operations = prepareOperations(otherNodeUUID);
        Operation lastConfirmedOperation = entryLog.getLastConfirmedOperation();

        AppendEntriesCommand command = AppendEntriesCommand.builder()
                .operations(operations)
                .currentIndex(!operations.isEmpty() ? operations.get(operations.size() - 1).getOperationIndex() : lastOperationIndex)
                .lastCommittedIndex(lastConfirmedOperation.getOperationIndex())
                .lastCommittedElection(lastConfirmedOperation.getElectionNumber())
                .lastIndex(lastOperationIndex)
                .build();

        Request request = Request.builder()
                .body(command)
                .electionNumber(electionNumber)
                .lastCommittedOperationIdx(entryLog.getLastConfirmedOperationIdx())
                .type(RequestType.APPEND_ENTRIES)
                .from(uuid)
                .to(otherNodeUUID)
                .build();

        return sendRequest(request, AppendEntriesResult.class);
    }

    private List<Operation> prepareOperations(UUID otherNodeUUID) {
        this.lastOperationIndex = entryLog.getLastOperationIdx();
        OtherNode node = otherNodes.get(otherNodeUUID);


        return entryLog.findOperations(node.getLastOperationIdx()).stream().map(Operation::clone).collect(Collectors.toList());

//        if (!node.isConsistent(this.electionNumber, this.lastOperationIndex)) {
//            return entryLog.findOperations(node.getLastOperationIdx());
//        } else {
//            return Collections.emptyList();
//        }
    }

    private void confirmEntriesLog() {
        List<Operation> operations = this.entryLog.confirmAll();
        updateCurrentState(operations);

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
        this.otherNodes = allNodes.stream()
                .filter(tmp -> !tmp.equals(uuid))
                .map(OtherNode::new)
                .collect(Collectors.toMap(OtherNode::getUuid, node -> node));
    }

    public AppendEntriesResult appendEntries(Request request) throws InterruptedException {
        if (disabled) {
            log.info(uuid + " is DISABLED");
            Thread.sleep(ERROR_TIMEOUT);
            return null;
        }

        AppendEntriesCommand command = (AppendEntriesCommand) request.getBody();
        if (command.getOperations().size() > 0 && !this.state.equals(NodeState.LEADER)) {
            log.info("NORMAL NODE =======> " + uuid);
        }

        if (request.getElectionNumber() < this.electionNumber) {
            throw new IllegalStateException(prepareErrMsgForOutdateLeader(request));
        }


        Operation lastConfirmedOperation = entryLog.getLastConfirmedOperation();
        if (lastConfirmedOperation.getOperationIndex() > command.getLastCommittedIndex()) {
            throw new IllegalStateException(prepareMessageForLeaderWithOutdateEntryLog(request, command, lastConfirmedOperation));
        }

        if (request.getElectionNumber() == this.electionNumber && NodeState.CANDIDATE.equals(this.state)) {
            this.state = NodeState.FOLLOWER;
            log.info("Node " + uuid + " was candidate but become follower - " + this.electionNumber + " " + request.getElectionNumber());
        }

        if (NodeState.LEADER.equals(state)) {
            if (request.getElectionNumber() > this.electionNumber) {
                log.info("Node " + uuid + " become follower - " + this.electionNumber + " " + request.getElectionNumber());
                this.electionNumber = request.getElectionNumber();
                this.state = NodeState.FOLLOWER;
            }
        }

        entryLog.removeExpiredOperations(request.getElectionNumber());

        resetElectionTimer();

        this.leaderUUID = request.getFrom();
        this.votedFor = null;
        this.electionNumber = request.getElectionNumber();

        if (command.getLastIndex() == lastOperationIndex) {
            log.info("NODE " + uuid + "command.getLastIndex() == lastOperationIndex");
            appendNewEntries(command);
        } else if (command.getLastIndex() > lastOperationIndex) {
            log.info("NODE " + uuid + "command.getLastIndex() > lastOperationIndex");
            appendNewEntries(command);
        } else if (command.getLastIndex() < lastOperationIndex) {
            log.info("NODE " + uuid + "cop.getOperationIndex() > command.getLastIndex()");
            entryLog.removeIf(op -> op.getOperationIndex() > command.getLastIndex());
            appendNewEntries(command);
        }

        confirmPotentialOperations(command);


        return appendEntriesSuccess();
    }

    private String prepareMessageForLeaderWithOutdateEntryLog(Request request, AppendEntriesCommand command, Operation lastConfirmedOperation) {
        return String.format("Node[%s] -> CANNOT ACCEPT APPEND ENTRIES FROM NODE[%s] - MY LAST COMMITTED OPERATION[%s] IS GREATER THEN YOURS[%s]",
                this.uuid,
                request.getFrom(),
                lastConfirmedOperation.getOperationIndex(),
                command.getLastCommittedIndex());
    }

    private void confirmPotentialOperations(AppendEntriesCommand command) {
        List<Operation> operations = this.entryLog.confirm(command.getLastCommittedIndex());
        updateCurrentState(operations);
    }

    private AppendEntriesResult appendEntriesFailure() {
        return AppendEntriesResult.builder()
                .success(false)
                .electionNumber(electionNumber)
                .lastIndex(lastOperationIndex)
                .build();
    }

    private AppendEntriesResult appendEntriesSuccess() {
        return AppendEntriesResult.builder()
                .success(true)
                .electionNumber(electionNumber)
                .lastIndex(lastOperationIndex)
                .build();
    }

    private void appendNewEntries(AppendEntriesCommand command) {
        entryLog.addAll(command.getOperations());
        lastOperationIndex = command.getCurrentIndex();
    }

    private void updateCurrentState(List<Operation> operations) {
        for (val op : operations) {
            switch (op.getType()) {

                case ADD:
                case MODIFY:
                    records.put(op.getRecordId(), new Record(op.getRecordId(), op.getRecordValue()));
                    break;
                case DELETE:
                    records.remove(op.getRecordId());
                    break;
            }


        }
    }

    private String prepareErrMsgForOutdateLeader(Request request) {
        return "You are leader of election " + request.getElectionNumber() + " but current election is " + this.electionNumber;
    }

    private String prepareErrMsgForOutdateCandidate(Request request) {
        return "You are candidate of election " + request.getElectionNumber() + " but current election is " + this.electionNumber;
    }

    public Response<Object> sendRequest(Request request) {
        return sendRequest(request, Object.class);
    }

    public <T extends Object> Response<T> sendRequest(Request request, Class<T> classOfT) {
        try {

            Future<?> future = executor.submit(() -> Network.getInstance().handle(request));
            Object result = future.get(heartbeatIntervalTimeInMs - 10, TimeUnit.MILLISECONDS);

            if (RequestType.APPEND_ENTRIES.equals(request.getType())) {
                if (result == null) {
                    return Response.<T>builder()
                            .nodeId(request.getTo())
                            .success(false)
                            .build();
                }

                AppendEntriesResult appendEntriesResult = (AppendEntriesResult) result;

                return Response.<T>builder()
                        .nodeId(request.getTo())
                        .success(appendEntriesResult.isSuccess())
                        .body((T) appendEntriesResult)
                        .build();
            }

            return Response.<T>builder()
                    .nodeId(request.getTo())
                    .success(true)
                    .body((T) result)
                    .build();

        } catch (Exception ex) {
            log.error(ex);
            return (Response<T>) Response.builder().nodeId(request.getTo()).msg(ex.getMessage()).success(false).build();
        }
    }

    synchronized public void disable() {
        log.info(uuid + " has been disabled");
        disabled = true;
        timer.cancel();

        log.info(uuid + " DISABLED ? " + disabled);
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


        if (outOfElection(request) || outOfCommittedOperation(request)) {
            logAboutBecomingFollower(request);

            this.electionNumber = request.getElectionNumber();
            this.state = NodeState.FOLLOWER;
            this.votedFor = request.getFrom();

            resetElectionTimer();
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

    private void logAboutBecomingFollower(Request request) {
        //lco -> last commited operation

        log.info(String.format("NODE[%s] -> new election started %s lco %s My election %s lco %s give vote and become follower",
                this.uuid,
                request.getElectionNumber(),
                request.getLastCommittedOperationIdx(),
                electionNumber,
                entryLog.getLastConfirmedOperationIdx()));
    }

    private boolean outOfElection(Request request) {
        return request.getElectionNumber() > electionNumber;
    }

    private boolean outOfCommittedOperation(Request request) {
        return request.getLastCommittedOperationIdx() > entryLog.getLastConfirmedOperationIdx();
    }

    public void updateState(Request request) {

        if (NodeState.CANDIDATE.equals(this.state)) {
            throw new IllegalStateException("ELECTION is executed");
        }

        if (NodeState.FOLLOWER.equals(this.state)) {
            Request newRequest = Request.builder()
                    .body(request.getBody())
                    .electionNumber(electionNumber)
                    .lastCommittedOperationIdx(entryLog.getLastConfirmedOperationIdx())
                    .type(RequestType.REQUEST_VOTE)
                    .from(uuid)
                    .to(leaderUUID).build();

            Network.getInstance().handle(newRequest);
            return;
        }

        ClientUpdateCommand command = (ClientUpdateCommand) request.getBody();

        List<Operation> newOperations = command.getActions().stream().map(action -> Operation.builder()
                .operationId(UUID.randomUUID())
                .electionNumber(this.electionNumber)
                .type(action.getType())
                .recordId(action.getRecordId())
                .recordValue(action.getRecordValue())
                .build()).collect(Collectors.toList());

        entryLog.addAllAsLeader(newOperations);

        doLeaderJob();
    }

    public int getLastCommittedIdx() {
        return entryLog.getLastConfirmedOperationIdx();
    }

    synchronized public void switchState() {
        if (isDisabled()) {
            enable();
        } else {
            disable();
        }

    }

    public List<Operation> getOperations() {
        return this.entryLog.getAll();
    }
}
