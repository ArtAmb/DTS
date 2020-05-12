package dts;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class Response {
    private UUID nodeId;
    private boolean success;
    private String msg;
}
