package dts.core;

import lombok.Getter;

import java.util.List;

@Getter
public class AllRequestSummary {
    private final List<Response> responses;
    private final long size;
    private final long successes;

    public AllRequestSummary(List<Response> responses) {
        this.responses = responses;
        this.size = responses.size();
        this.successes = responses.stream().filter(Response::isSuccess).count();
    }


    public boolean isSuccessesCounterAtLeastHalf() {
        return successes >= (size / 2);

    }
}
