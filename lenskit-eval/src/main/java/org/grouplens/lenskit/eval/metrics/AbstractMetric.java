package org.grouplens.lenskit.eval.metrics;

/**
 * A simple metric base class that tracks the current evaluation.
 * @author Michael Ekstrand
 */
public abstract class AbstractMetric<E> implements Metric<E> {
    private E currentEvaluation;

    public void startEvaluation(E eval) {
        currentEvaluation = eval;
    }

    public void finishEvaluation() {
        currentEvaluation = null;
    }

    /**
     * Get the current evaluation, or {@code null} none is in progress.
     * @return The current evaluation.
     */
    protected E getCurrentEvaluation() {
        return currentEvaluation;
    }
}
