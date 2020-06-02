package dts.commands;

import dts.core.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Action {
    private OperationType type;
    private String recordId;
    private String recordValue;
}