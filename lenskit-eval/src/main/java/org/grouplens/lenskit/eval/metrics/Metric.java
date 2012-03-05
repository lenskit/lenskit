package org.grouplens.lenskit.eval.metrics;

/**
 * Base interface for metrics, which are used by evaluations to measure their results.
 * @param <E> The type of evaluation.
 * @author Michael Ekstrand
 * @since 0.10
 */
public interface Metric<E> {
    /**
     * Initialize the metric to accumulate evaluations for the specified evaluation.
     * This method is called before any accumulators are constructed. It is invalid
     * to start a metric twice without finishing it first.
     * @param eval The evaluation this metric is in use for.
     */
    void startEvaluation(E eval);
    /**
     * Finish the evaluation, releasing any resources allocated for it. This
     * will be called after all algorithms and data sets are run through the accumulator.
     * After this method, the metric should be ready to be used on another evaluation.
     * @review Is that really the behavior we want?
     */
    void finishEvaluation();
}
