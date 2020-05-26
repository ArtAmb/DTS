package dts.commands;

import dts.Operation;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AppendEntriesCommand {
    int lastIndex;
    int currentIndex;
    int lastCommittedIndex;

    List<Operation> operations;
}
