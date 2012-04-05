package org.grouplens.lenskit.eval;

/**
 * The exception thrown from the failure of call() in Commmand
 *
 * @author Shuo Chang<schang@cs.umn.edu>
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
