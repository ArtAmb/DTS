package dts.commands;

import dts.OperationType;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Action {
    private OperationType type;
    private String recordId;
    private String recordValue;
}