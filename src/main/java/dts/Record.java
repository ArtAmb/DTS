package dts;

import lombok.Builder;

import java.util.UUID;

@Builder
public class Record {
    private String id;
    private String value;

    public Record(String id, String value) {
        this.id = id;
        this.value = value;
    }
}
