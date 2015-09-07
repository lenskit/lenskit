package org.lenskit.eval.traintest;

/**
 * Exception thrown by evaluation actions.
 */
public class EvaluationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EvaluationException() {
    }

    public EvaluationException(String message) {
        super(message);
    }

    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvaluationException(Throwable cause) {
        super(cause);
    }
}
