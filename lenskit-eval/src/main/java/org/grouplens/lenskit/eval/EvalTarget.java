/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.eval;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Targets in an evaluator project.  This class extends Ant {@linkplain Target targets} with the
 * Future interface, providing access to the value of the last task executed.  Non-eval tasks
 * (standard Ant tasks) have null values.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class EvalTarget extends Target implements ListenableFuture<Object> {
    private static final Logger logger = LoggerFactory.getLogger(EvalTarget.class);
    protected final SettableFuture<Object> returnValue = SettableFuture.create();
    protected Future<?> lastTaskFuture = null;
    protected Task lastTask = null;

    @Override
    public void addTask(Task task) {
        logger.debug("adding task {} to {}", task, getName());
        super.addTask(task);
        lastTask = task;
        if (task instanceof EvalAntTask) {
            lastTaskFuture = ((EvalAntTask) task).getEvalTask();
        } else {
            lastTaskFuture = null;
        }
    }

    @Override
    public void execute() throws BuildException {
        try {
            logger.info("beginning execution of {}", getName());
            Stopwatch watch = new Stopwatch().start();
            super.execute();
            watch.stop();
            logger.info("{} finished in {}", getName(), watch);
            if (lastTaskFuture != null) {
                if (!lastTaskFuture.isDone()) {
                    logger.error("{}: future for task {} did not complete", getName(), lastTask);
                    returnValue.setException(new RuntimeException("task future didn't complete"));
                } else {
                    while (!returnValue.isDone()) {
                        try {
                            returnValue.set(lastTaskFuture.get());
                        } catch (ExecutionException ex) {
                            returnValue.setException(ex.getCause());
                        } catch (InterruptedException e) {
                            logger.warn("{}: task future get() was interrupted", getName());
                            /* try again */
                        }
                    }
                }
            } else {
                returnValue.set(null);
            }
        } catch (RuntimeException ex) {
            returnValue.setException(ex);
            throw ex;
        }
    }

    //region Future operations
    // These all delegate to the settable future.
    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
        return returnValue.get(timeout, unit);
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return returnValue.get();
    }

    @Override
    public boolean isDone() {
        return returnValue.isDone();
    }

    @Override
    public boolean isCancelled() {
        return returnValue.isCancelled();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return returnValue.cancel(mayInterruptIfRunning);
    }

    @Override
    public void addListener(Runnable listener, Executor exec) {
        returnValue.addListener(listener, exec);
    }
    //endregion
}
