package org.lenskit.specs;

public class NoSpecHandlerFound extends RuntimeException {
    private static final long serialVersionUID = -6649091618676264332L;

    public NoSpecHandlerFound() {
    }

    public NoSpecHandlerFound(String message) {
        super(message);
    }

    public NoSpecHandlerFound(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSpecHandlerFound(Throwable cause) {
        super(cause);
    }

    public NoSpecHandlerFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
