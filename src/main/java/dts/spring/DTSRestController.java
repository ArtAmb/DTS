package dts.spring;

import dts.core.NodeMachineState;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin("http://localhost:4200")
@RequestMapping
public class DTSRestController {
    private final SimulationService simulationService;

    @PostMapping("/simulation/start")
    public void startSimulation(@RequestBody StartSimulationCommand command) throws InterruptedException {
        simulationService.startSimulation(command);
    }

    @PostMapping("/simulation/stop")
    public void stopSimulation() throws InterruptedException {
        simulationService.stopSimulation();
    }

    @GetMapping("/simulation/nodes")
    public List<NodeMachineState> getAllNodes() {
        return simulationService.readAll();
    }

    @GetMapping("/simulation/is-running")
    public SimulationRunningDTO isSimulationRunning() {
        return simulationService.isRunning();
    }

}

