package dts.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dts.commands.Action;
import dts.commands.ClientUpdateCommand;
import dts.spring.SimulationRunningDTO;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lombok.val;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Log4j
public class Environment {
    private static final Environment instance = new Environment();
    @Getter
    private volatile boolean successRestriction = false;

    public static Environment getInstance() {
        return instance;
    }

    private Random rand = new Random();
    private ConcurrentHashMap<UUID, Node> uuidToNodeMap = new ConcurrentHashMap<>();
    private Gson preattyGSON = new GsonBuilder().setPrettyPrinting().create();
    private boolean simulationRunning = false;

    private Thread chaosMonkeyThread = null;
    private Thread findAndDisableLeaderThread = null;

    private TestingAlgorithm testingAlgorithm = TestingAlgorithm.NONE;

    public void createNewNode() {
        Node node = Node.createNew();


        uuidToNodeMap.put(node.getUuid(), node);
    }

    Node getNode(UUID uuid) {
        return uuidToNodeMap.get(uuid);
    }


    public void runSimulation(int nodeNumber) throws InterruptedException {
        for (int i = 0; i < nodeNumber; ++i) {
            createNewNode();
        }

        Set<UUID> allNodes = uuidToNodeMap.keySet();
        uuidToNodeMap.values().forEach(node -> node.fillOtherNodes(allNodes));
        uuidToNodeMap.values().forEach(this::print);
        uuidToNodeMap.values().forEach(Node::start);

        simulationRunning = true;


//        chaosMonkey(nodeNumber);
//        findAndDisableLeader();


//        startLoggingState();
//        startRandomUpdating();
    }

    public void stopSimulation() {
        simulationRunning = false;
        testingAlgorithm = TestingAlgorithm.NONE;
        stopChaosMonkey();
        stopFindAndDisableLeader();

        uuidToNodeMap.values().forEach(Node::disable);
        uuidToNodeMap.clear();

        log.info("SIMULATION STOPPED");
    }

    public SimulationRunningDTO isSimulationRunning() {
        return SimulationRunningDTO.builder()
                .running(this.simulationRunning)
                .nodesNumber(this.uuidToNodeMap.keySet().size())
                .build();
    }

    private void startRandomUpdating() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    update();
                } catch (InterruptedException e) {
                    log.info(e);
                    throw new IllegalStateException(e);
                }
            }

        }).start();
    }

    private void startLoggingState() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                    logAllNodesStates();
                } catch (InterruptedException e) {
                    log.info(e);
                    throw new IllegalStateException(e);
                }
            }
        }).start();
    }

    public void findAndDisableLeader() {
        this.findAndDisableLeaderThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(1000);
                    Node leader = uuidToNodeMap.values().stream().filter(node -> NodeState.LEADER.equals(node.getState())).findFirst().orElse(null);

                    if (leader == null) {
                        Thread.sleep(1000);
                        continue;
                    }

                    leader.disable();
                    Thread.sleep(1000);
                    leader.enable();
                }
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        });

        testingAlgorithm = TestingAlgorithm.FIND_AND_DISABLE_LEADER;
        this.findAndDisableLeaderThread.start();
    }

    public void stopFindAndDisableLeader() {
        this.testingAlgorithm = TestingAlgorithm.NONE;
        if (this.findAndDisableLeaderThread != null) {
            this.findAndDisableLeaderThread.interrupt();
        }
    }

    public TestingAlgorithm findTestingAlgorithm() {
        return this.testingAlgorithm;
    }

    private void print(Node node) {
        log.info(String.format("NODE %s - timeout %s", node.getUuid(), node.getTimeoutInMs()));
    }

    public void chaosMonkey() {
        this.chaosMonkeyThread = new Thread(() -> {
            try {
                Thread.sleep(1000);

                while (true) {
                    disableRandomNode(uuidToNodeMap.keySet().size());
                }

            } catch (InterruptedException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

        });

        testingAlgorithm = TestingAlgorithm.CHAOS_MONKEY;
        this.chaosMonkeyThread.start();
    }

    public void stopChaosMonkey() {
        this.testingAlgorithm = TestingAlgorithm.NONE;
        if (this.chaosMonkeyThread != null) {
            this.chaosMonkeyThread.interrupt();
        }
    }

    void disableRandomNode(int nodeNumber) throws InterruptedException {

        int nodeNum = new Random().nextInt(nodeNumber);
        Node node = new ArrayList<>(uuidToNodeMap.values()).get(nodeNum);
        node.disable();

        Thread.sleep(1000);
        node.enable();
    }


    public void logAllNodesStates() {
        log.info("================ ALL STATES =====================");
        log.info(preattyGSON.toJson(getNodeStates()));
    }

    public List<NodeMachineState> getNodeStates() {
        return this.uuidToNodeMap.values().stream().map(node -> {
            return NodeMachineState.builder()
                    .nodeId(node.getUuid())
                    .nodeState(node.getState())
                    .electionNumber(node.getElectionNumber())
                    .lastOperationIdx(node.getLastOperationIndex())
                    .lastCommittedIdx(node.getLastCommittedIdx())
                    .votedFor(node.getVotedFor())
                    .disabled(node.isDisabled())
                    .records(node.getRecords())
                    .operations(node.getOperations())
                    .build();
        }).collect(Collectors.toList());
    }


    public void update() {
        Action action = Action.builder().recordId(UUID.randomUUID().toString()).recordValue(UUID.randomUUID().toString()).type(OperationType.ADD).build();
        update(action);
    }

    public void update(Action action) {

        ClientUpdateCommand command = ClientUpdateCommand.builder().actions(Collections.singletonList(action)).build();
        Node leader = uuidToNodeMap.values()
                .stream()
                .filter(node -> !node.isDisabled())
                .filter(node -> NodeState.LEADER.equals(node.getState())).findFirst().orElseThrow(() -> new IllegalStateException("No leader"));

        Request req = Request.builder().type(RequestType.UPDATE).body(command).to(leader.getUuid()).build();
        Network.getInstance().handle(req);
    }

    public void update(UUID nodeId, Action action) {
        ClientUpdateCommand command = ClientUpdateCommand.builder().actions(Collections.singletonList(action)).build();
        Node node = uuidToNodeMap.get(nodeId);

        Request req = Request.builder().type(RequestType.UPDATE).body(command).to(node.getUuid()).build();
        Network.getInstance().handle(req);
    }

    public void disableNode(UUID nodeId) {
        uuidToNodeMap.get(nodeId).disable();
    }

    public void enableNode(UUID nodeId) {
        uuidToNodeMap.get(nodeId).enable();
    }

    public void switchNode(UUID nodeId) {
        uuidToNodeMap.get(nodeId).switchState();
    }

    public synchronized void updateSuccessRestriction(Boolean enable) {
        this.successRestriction = enable;
    }


    public synchronized void twoLeaders() {
        findPairNodesWithSameElection().forEach(Node::forceLeader);
    }

    public synchronized void twoCandidates() {
        findPairNodesWithSameElection().forEach(Node::forceCandidate);
    }

    private synchronized List<Node> findPairNodesWithSameElection() {
        Map<Integer, List<Node>> map = uuidToNodeMap.values().stream()
                .collect(Collectors.groupingBy(Node::getElectionNumber));

        val nodesWithSameElection = map.entrySet()
                .stream().filter(es -> es.getValue().size() > 1)
                .collect(Collectors.toList());

        if (nodesWithSameElection.isEmpty()) {
            throw new IllegalStateException("All nodes have different election number");
        }

        Map.Entry<Integer, List<Node>> maxElectionNodes = nodesWithSameElection.stream()
                .max(Comparator.comparingInt(Map.Entry::getKey))
                .orElseThrow(() -> new IllegalStateException("Max election not found"));

        return maxElectionNodes.getValue();
    }
}
