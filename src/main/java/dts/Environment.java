package dts;

import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Log4j
public class Environment {
    private static final Environment instance = new Environment();

    public static Environment getInstance() {
        return instance;
    }

    private Random rand = new Random();
    private ConcurrentHashMap<UUID, Node> uuidToNodeMap = new ConcurrentHashMap<>();

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


//        chaosMonkey(nodeNumber);
        findAndDisableLeader();
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


}
