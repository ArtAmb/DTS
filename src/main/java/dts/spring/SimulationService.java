package dts.spring;

import dts.commands.Action;
import dts.core.Environment;
import dts.core.NodeMachineState;
import dts.core.TestingAlgorithm;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SimulationService {

    private final Environment environment = Environment.getInstance();

    public void startSimulation(StartSimulationCommand command) throws InterruptedException {
        environment.runSimulation(command.getNodesNumber());
    }

    public void stopSimulation() {
        environment.stopSimulation();
    }

    public List<NodeMachineState> readAll() {
        return environment.getNodeStates();
    }

    public SimulationRunningDTO isRunning() {
        return environment.isSimulationRunning();
    }

    public void startChaosMonkey() {
        environment.chaosMonkey();
    }

    public void stopChaosMonkey() {
        environment.stopChaosMonkey();
    }

    public void findAndDisableLeader() {
        environment.findAndDisableLeader();
    }

    public void stopFindAndDisableLeader() {
        environment.stopFindAndDisableLeader();
    }

    public TestingAlgorithm findTestingAlgorithm() {
        return environment.findTestingAlgorithm();
    }

    public void disableNode(UUID nodeId) {
        environment.disableNode(nodeId);
    }

    public void enableNode(UUID nodeId) {
        environment.enableNode(nodeId);
    }

    public void switchNode(UUID nodeId) {
        environment.switchNode(nodeId);
    }

    public void updateRecord(Action action) {
        environment.update(action);
    }

    public void updateSuccessRestriction(Boolean enable) {
        environment.updateSuccessRestriction(enable);
    }

    public Boolean isRestrictionEnabled() {
        return environment.isSuccessRestriction();
    }

    public void updateRecord(UUID nodeId, Action action) {
        environment.update(nodeId, action);
    }
}
