package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.knn.params.ModelSize;
import org.grouplens.lenskit.transform.threshold.Threshold;

import javax.inject.Inject;

/**
 * Factory for use in instantiating a {@link SimilarityMatrixAccumulator}.
 */
public class SimilarityMatrixAccumulatorFactory {

    private final int modelSize;
    private final Threshold threshold;

    @Inject
    public SimilarityMatrixAccumulatorFactory(@ModelSize int modelSize,
                                              Threshold threshold) {
        this.modelSize = modelSize;
        this.threshold = threshold;
    }

    public SimilarityMatrixAccumulator create(LongSortedSet itemUniverse) {
        return new SimilarityMatrixAccumulator(modelSize, itemUniverse, threshold);
    }

}
