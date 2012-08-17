/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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
