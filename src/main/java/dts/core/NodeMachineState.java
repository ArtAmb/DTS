package dts.core;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
@Value
public class NodeMachineState {
    private final UUID nodeId;
    private final NodeState nodeState;

    private final int lastCommittedIdx;
    private final int lastOperationIdx;
    private final int electionNumber;
    private final UUID votedFor;

    private final boolean disabled;

    private final Map<String, Record> records;
    private final List<Operation> operations;
}
