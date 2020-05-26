package dts;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.UUID;

@Builder
@Getter
public class Response<T> {
    private UUID nodeId;
    private boolean success;
    private String msg;

    private T body;

    public ResponseMetadata toMetadata() {
        return ResponseMetadata.builder()
                .nodeId(nodeId)
                .success(success)
                .msg(msg)
                .build();
    }
}

@Builder
@Value
class ResponseMetadata {
    private UUID nodeId;
    private boolean success;
    private String msg;
}
