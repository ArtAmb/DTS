package dts;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class AppendEntriesResult {
    private boolean success;
    private final int lastIndex;
    private final int electionNumber;
}
