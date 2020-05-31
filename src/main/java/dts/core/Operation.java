package dts.core;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Builder
@Getter
public class Operation {
    private final UUID operationId;
    private int prevIndex;
    private int operationIndex;
    private int electionNumber;
    private OperationState state;


    private OperationType type;
    private String recordId;
    private String recordValue;


    public void confirm() {
        state = OperationState.CONFIRMED;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Operation) {
            return Objects.equals(operationId, ((Operation) obj).operationId);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(operationId);
    }

    public void updateIndex(int idx) {
        this.operationIndex = idx;
        this.prevIndex = idx - 1;
    }

    public boolean isPotential() {
        return OperationState.POTENTIAL.equals(state);
    }

    public Operation markAsPotential() {
        this.state = OperationState.POTENTIAL;
        return this;
    }
}
