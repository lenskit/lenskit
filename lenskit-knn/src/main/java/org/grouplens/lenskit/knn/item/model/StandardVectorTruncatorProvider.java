package org.grouplens.lenskit.knn.item.model;

import com.google.common.base.Objects;
import org.grouplens.lenskit.knn.item.ItemSimilarityThreshold;
import org.grouplens.lenskit.knn.item.ModelSize;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.transform.truncate.ThresholdTruncator;
import org.grouplens.lenskit.transform.truncate.TopNTruncator;
import org.grouplens.lenskit.transform.truncate.VectorTruncator;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Configure a vector truncator using the standard item-item model build logic.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class StandardVectorTruncatorProvider implements Provider<VectorTruncator> {
    private final Threshold threshold;
    private final int modelSize;

    /**
     * Construct a new vector truncator provider.
     * @param thresh A threshold for filtering item similarities.
     * @param msize The maximum number of neighbors to retain for each item.
     */
    @Inject
    public StandardVectorTruncatorProvider(@ItemSimilarityThreshold Threshold thresh,
                                           @ModelSize int msize) {
        threshold = thresh;
        modelSize = msize;
    }

    @Override
    public VectorTruncator get() {
        if (modelSize > 0) {
            return new TopNTruncator(modelSize, threshold);
        } else {
            return new ThresholdTruncator(threshold);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(StandardVectorTruncatorProvider.class)
                      .add("modelSize", modelSize)
                      .add("threshold", threshold)
                      .toString();
    }
}
