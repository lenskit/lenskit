package org.grouplens.lenskit.eval.metrics;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Interface for metric accumulators.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface MetricAccumulator {
    /**
     * Finish accumulating results for the experiment and return the result entries.
     * @return The result row for this experiment, to be put in the output table.
     */
    @Nonnull
    List<Object> finish();
}
