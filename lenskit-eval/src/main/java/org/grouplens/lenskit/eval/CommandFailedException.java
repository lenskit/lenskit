package org.grouplens.lenskit.eval;

/**
 * Created by IntelliJ IDEA.
 * User: schang
 * Date: 3/28/12
 * Time: 5:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandFailedException extends Exception{
    private static final long serialVersionUID = -9073424874249517829L;

    public CommandFailedException() {}
    
    public CommandFailedException(String message) {
        super(message);
    }
    
    public CommandFailedException(Exception cause) {
        super(cause);
    }
    
    public CommandFailedException(String message, Exception cause) {
        super(message, cause);
    }
}
