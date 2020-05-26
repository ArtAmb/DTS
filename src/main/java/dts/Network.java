package dts;

import lombok.extern.log4j.Log4j;

@Log4j
public class Network {
    private static final Network instance = new Network();
    private final Environment env = Environment.getInstance();

    public static Network getInstance() {
        return instance;
    }


    public Object handle(Request request) {
        try {
            log.info(request);
            Node node = env.getNode(request.getTo());
            switch (request.getType()) {
                case APPEND_ENTRIES: {
                    return node.appendEntries(request);
                }

                case REQUEST_VOTE: {
                    node.requestVote(request);
                    break;
                }

                case SYNCHRONIZED:
                    break;
                case READ_ALL:
                    break;
                case UPDATE:
                    node.updateState(request);
                    break;
            }

            return null;
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }
}
