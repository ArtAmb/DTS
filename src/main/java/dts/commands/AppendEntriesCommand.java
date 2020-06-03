package dts.commands;

import dts.core.Operation;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AppendEntriesCommand {
    int lastIndex;
    int currentIndex;
    int lastCommittedIndex;
    int lastCommittedElection;

    List<Operation> operations;
}
