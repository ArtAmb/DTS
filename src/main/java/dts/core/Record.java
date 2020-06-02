package dts.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class Record {
    private String id;
    private String value;

    public Record(String id, String value) {
        this.id = id;
        this.value = value;
    }
}
