package dts.spring;

import dts.commands.Action;
import dts.core.NodeMachineState;
import dts.core.TestingAlgorithm;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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


    @PostMapping("/simulation/chaos-monkey/start")
    public void startChaosMonkey() {
        simulationService.startChaosMonkey();
    }

    @PostMapping("/simulation/chaos-monkey/stop")
    public void stopChaosMonkey() {
        simulationService.stopChaosMonkey();
    }

    @PostMapping("/simulation/find-and-disable-leader/start")
    public void startFindAndDisableLeader() {
        simulationService.findAndDisableLeader();
    }

    @PostMapping("/simulation/find-and-disable-leader/stop")
    public void stopFindAndDisableLeader() {
        simulationService.stopFindAndDisableLeader();
    }

    @GetMapping("/simulation/testing-algorithm")
    public TestingAlgorithm findTestingAlgorithm() {
        return simulationService.findTestingAlgorithm();
    }


    @PostMapping("/simulation/nodes/{nodeId}/disable")
    public void disableNode(@PathVariable UUID nodeId) {
        simulationService.disableNode(nodeId);
    }

    @PostMapping("/simulation/nodes/{nodeId}/enable")
    public void enableNode(@PathVariable UUID nodeId) {
        simulationService.enableNode(nodeId);
    }

    @PostMapping("/simulation/nodes/{nodeId}/switch")
    public void switchNode(@PathVariable UUID nodeId) {
        simulationService.switchNode(nodeId);
    }


    @PostMapping("/simulation/record/save")
    public void updateRecord(@RequestBody Action action) {
        simulationService.updateRecord(action);
    }

    @PostMapping("/simulation/record/save/node/{nodeId}/")
    public void updateRecordForNode(@PathVariable UUID nodeId, @RequestBody Action action) {
        simulationService.updateRecord(nodeId, action);
    }

    @PostMapping("/simulation/raft/success-restriction/{enable}")
    public void updateSuccessRestriction(@PathVariable Boolean enable) {
        simulationService.updateSuccessRestriction(enable);
    }

    @GetMapping("/simulation/raft/success-restriction")
    public Boolean isRestrictionEnabled() {
        return simulationService.isRestrictionEnabled();
    }


}

