package org.grouplens.lenskit.eval;

/**
 * Created by IntelliJ IDEA.
 * User: schang
 * Date: 3/20/12
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
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
