package dts.commands;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class ClientUpdateCommand {
    private final List<Action> actions;
}

