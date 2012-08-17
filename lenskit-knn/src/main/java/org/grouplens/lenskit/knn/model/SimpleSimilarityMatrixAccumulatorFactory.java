package org.grouplens.lenskit.knn.model;

import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.knn.params.ModelSize;
import org.grouplens.lenskit.transform.threshold.Threshold;

import javax.inject.Inject;

/**
 * Implementation of {@link SimilarityMatrixAccumulatorFactory} which creates
 * and returns a {@link SimpleSimilarityMatrixAccumulator}
 */
@Shareable
public class SimpleSimilarityMatrixAccumulatorFactory implements SimilarityMatrixAccumulatorFactory {

    private final int modelSize;
    private final Threshold threshold;

    @Inject
    public SimpleSimilarityMatrixAccumulatorFactory(@ModelSize int modelSize,
                                                    Threshold threshold) {
        this.modelSize = modelSize;
        this.threshold = threshold;
    }

    /**
     * Creates and returns a SimpleSimilarityMatrixAccumulator.
     * @param itemUniverse The universe items the accumulator will accumulate.
     * @return a simple SimilarityMatrixAccumulator
     */
    public SimilarityMatrixAccumulator create(LongSortedSet itemUniverse) {
        return new SimpleSimilarityMatrixAccumulator(modelSize, itemUniverse, threshold);
    }

}
