package dts;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class Operation {
    private UUID operationId;
    private int electionNumber;
    private OperationType type;


    private UUID recordId;
    private String recordValue;
}
