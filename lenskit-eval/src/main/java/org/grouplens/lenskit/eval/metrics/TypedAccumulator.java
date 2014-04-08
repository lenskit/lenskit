package org.grouplens.lenskit.eval.metrics;

/**
 * A base implementation of {@link MetricAccumulator} that uses typed results.
 *
 * @param <T> The result type.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public interface TypedAccumulator<T> {
    /**
     * Finish accumulating and return a result.
     * @return The computed result.
     * @see MetricAccumulator
     */
    T finish();
}
