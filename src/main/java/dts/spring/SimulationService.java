package dts.spring;

import dts.core.Environment;
import dts.core.NodeMachineState;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
