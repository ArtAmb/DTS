package dts.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.util.List;

@Getter
@Log4j
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
        log.info(successes + " " + (size / 2));
        return successes >= (size / 2);

    }
}
