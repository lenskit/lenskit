package org.grouplens.lenskit.eval.config;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.grouplens.lenskit.eval.EvalTask;
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
    protected final SettableFuture<Object> returnValue = SettableFuture.create();
    protected Future<?> lastTaskFuture = null;
    protected Task lastTask = null;
    private transient volatile Logger logger;

    protected Logger getLogger() {
        if (logger == null) {
            String name = String.format("%s(%s)".format(getClass().getName(), getName()));
            logger = LoggerFactory.getLogger(name);
        }
        return logger;
    }

    @Override
    public void addTask(Task task) {
        getLogger().debug("adding task {}", task);
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
            getLogger().info("beginning execution");
            Stopwatch watch = new Stopwatch().start();
            super.execute();
            watch.stop();
            getLogger().info("execution finished in {}", watch);
            if (lastTaskFuture != null) {
                if (!lastTaskFuture.isDone()) {
                    getLogger().error("{}: future for task {} did not complete", getName(), lastTask);
                    returnValue.setException(new RuntimeException("task future didn't complete"));
                } else {
                    while (!returnValue.isDone()) {
                        try {
                            returnValue.set(lastTaskFuture.get());
                        } catch (ExecutionException ex) {
                            returnValue.setException(ex.getCause());
                        } catch (InterruptedException e) {
                            getLogger().warn("task future get() was interrupted");
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
