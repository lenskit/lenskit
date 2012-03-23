package org.grouplens.lenskit.eval;

import org.grouplens.lenskit.eval.cli.EvalOptions;

/**
 * Created by IntelliJ IDEA.
 * User: schang
 * Date: 3/20/12
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class GlobalEvalOptions {
    private final boolean force;
    private int threadCount = 1;
    private IsolationLevel isolation = IsolationLevel.NONE;

    public GlobalEvalOptions(EvalOptions opt) {
        force = opt.forcePrepare();
        threadCount = opt.getThreadCount();
        isolation = opt.getIsolation();
    }

    public boolean isForce() {
        return force;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public IsolationLevel getIsolation() {
        return isolation;
    }

    public void setIsolation(IsolationLevel isolation) {
        this.isolation = isolation;
    }
}
