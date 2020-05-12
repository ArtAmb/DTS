package dts.commands;

import dts.Operation;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AppendEntriesCommand {
    List<Operation> operations;
}
