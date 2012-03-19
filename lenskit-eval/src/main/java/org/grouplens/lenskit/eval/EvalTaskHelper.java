package org.grouplens.lenskit.eval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: schang
 * Date: 3/17/12
 * Time: 2:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class EvalTaskHelper {
    private static final Logger logger = LoggerFactory.getLogger(EvalTaskHelper.class);

    private int threadCount = 1;
    private IsolationLevel isolationLevel = IsolationLevel.NONE;

    private EvalListenerManager listeners = new EvalListenerManager();

    /**
     * Construct an evaluation runner from an evaluator name and configuration.
     *
     *
     */
    public EvalTaskHelper() {}

    /**
     * Get the eval runner thread count.
     * @return The number of concurrent evaluations to allow.
     * @see #setThreadCount(int)
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * Set eval runner thread count.
     *
     * @param threads The number of concurrent evaluations to allow. This has no
     *        impact on the number of threads used for building recommenders, if
     *        multithreaded recommenders are used.  If 0, then all available
     *        processors are used (from {@link Runtime#availableProcessors()}).
     *        The default is 1.
     * @return The evaluation runner for chaining.
     */
    public EvalTaskHelper setThreadCount(int threads) {
        threadCount = threads;
        return this;
    }

    /**
     * Get the evaluation isolation level.
     * @see #setIsolationLevel(IsolationLevel)
     */
    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    /**
     * Set the isolation level for this evaluation.
     *
     * @param level The isolation level to use. The default is
     *        {@link IsolationLevel#NONE}.
     * @return The evaluation runner for chaining.
     */
    public EvalTaskHelper setIsolationLevel(IsolationLevel level) {
        isolationLevel = level;
        return this;
    }

    public void addListener(EvaluationListener listener) {
        listeners.addListener(listener);
    }

    public void removeListener(EvaluationListener listener) {
        listeners.removeListener(listener);
    }


    public JobGroupExecutor getExecutor() throws RuntimeException{
        int nthreads = threadCount;
        if (nthreads <= 0) {
            nthreads = Runtime.getRuntime().availableProcessors();
        }

        JobGroupExecutor exec;
        switch (isolationLevel) {
            case NONE:
                exec = new MergedJobGroupExecutor(nthreads, listeners);
                break;
            case JOB_GROUP:
                exec = new SequentialJobGroupExecutor(nthreads, listeners);
                break;
            default:
                throw new RuntimeException("Invalid isolation level " + isolationLevel);
        }

        return exec;
    }
}
