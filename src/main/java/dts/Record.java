package dts;

import lombok.Builder;

import java.util.UUID;

@Builder
public class Record {
    private UUID id;
    private RecordType type;
    private String value;
}
