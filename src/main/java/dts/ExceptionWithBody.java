package dts;

public class ExceptionWithBody extends RuntimeException {

    private final Object body;

    public ExceptionWithBody(String msg, Object body) {
        super(msg);
        this.body = body;
    }

    public ExceptionWithBody(String msg, Object body, Exception ex) {
        super(msg, ex);
        this.body = body;
    }

    public Object getBody() {
        return body;
    }
}
