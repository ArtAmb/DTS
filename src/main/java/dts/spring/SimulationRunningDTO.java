package dts.spring;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class SimulationRunningDTO {
    private final boolean running;
    private final int nodesNumber;
}
