package dts.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dts.commands.Action;
import dts.commands.ClientUpdateCommand;
import dts.spring.SimulationRunningDTO;
import lombok.extern.log4j.Log4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Log4j
public class Environment {
    private static final Environment instance = new Environment();

    public static Environment getInstance() {
        return instance;
    }

    private Random rand = new Random();
    private ConcurrentHashMap<UUID, Node> uuidToNodeMap = new ConcurrentHashMap<>();
    private Gson preattyGSON = new GsonBuilder().setPrettyPrinting().create();
    private boolean simulationRunning = false;

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
        uuidToNodeMap.values().forEach(Node::disable);
        uuidToNodeMap.clear();

        simulationRunning = false;

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

    private void findAndDisableLeader() throws InterruptedException {

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
    }

    private void print(Node node) {
        log.info(String.format("NODE %s - timeout %s", node.getUuid(), node.getTimeoutInMs()));
    }

    void chaosMonkey(int nodeNumber) throws InterruptedException {
        Thread.sleep(1000);

        while (true) {
            disableRandomNode(nodeNumber);
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
                    .build();
        }).collect(Collectors.toList());
    }


    public void update() {
        Action actions = Action.builder().recordId(UUID.randomUUID().toString()).recordValue(UUID.randomUUID().toString()).type(OperationType.ADD).build();
        ClientUpdateCommand command = ClientUpdateCommand.builder().actions(Collections.singletonList(actions)).build();
        Node leader = uuidToNodeMap.values()
                .stream()
                .filter(node -> NodeState.LEADER.equals(node.getState())).findFirst().orElseThrow(() -> new IllegalStateException("No leader"));

        Request req = Request.builder().type(RequestType.UPDATE).body(command).to(leader.getUuid()).build();
        Network.getInstance().handle(req);
    }
}
