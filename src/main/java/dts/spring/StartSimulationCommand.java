package dts.spring;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class StartSimulationCommand {
    private int nodesNumber;
}
