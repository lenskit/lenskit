package org.grouplens.lenskit.eval;

import java.util.concurrent.Callable;

/**
 * A command of evaluation task, which is exposed to the evaluation configuration script.
 * So the command can be called dynamically.
 *
 * @author Shuo Chang <schang@cs.umn.edu>
 */
public interface Command<T> extends Callable<T> {

    String getName();

    T call() throws CommandFailedException;
}
