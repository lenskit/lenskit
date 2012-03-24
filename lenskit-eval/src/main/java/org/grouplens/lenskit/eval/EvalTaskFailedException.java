package org.grouplens.lenskit.eval;

/**
 * The exception thrown when error occurs in execution of the evaluation task
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public class EvalTaskFailedException extends Exception{

    private static final long serialVersionUID = -9073424874249517829L;

    public EvalTaskFailedException() {
    }

    public EvalTaskFailedException(String message) {
        super(message);
    }

    public EvalTaskFailedException(Throwable cause) {
        super(cause);
    }

    public EvalTaskFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
