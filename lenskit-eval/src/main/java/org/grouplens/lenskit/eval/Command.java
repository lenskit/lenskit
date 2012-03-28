package org.grouplens.lenskit.eval;

import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: schang
 * Date: 3/28/12
 * Time: 5:35 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Command extends Callable<Void> {
    
    Void call() throws CommandFailedException;
}
