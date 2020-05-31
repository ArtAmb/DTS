package dts.core;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Builder
@Value
public class Request {
    int electionNumber;

    UUID from;
    UUID to;

    RequestType type;
    Object body;
}
