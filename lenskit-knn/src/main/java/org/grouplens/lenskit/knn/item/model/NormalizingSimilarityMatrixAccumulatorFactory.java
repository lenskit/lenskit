/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.knn.params.ModelSize;
import org.grouplens.lenskit.transform.normalize.ItemVectorNormalizer;
import org.grouplens.lenskit.transform.threshold.Threshold;

import javax.inject.Inject;

/**
 * Implementation of {@link SimilarityMatrixAccumulatorFactory} which creates
 * and returns a {@link NormalizingSimilarityMatrixAccumulator}.
 */
@Shareable
public class NormalizingSimilarityMatrixAccumulatorFactory implements SimilarityMatrixAccumulatorFactory {

    private final Threshold threshold;
    private final ItemVectorNormalizer normalizer;
    private final int modelSize;


    @Inject
    public NormalizingSimilarityMatrixAccumulatorFactory(Threshold threshold,
                                                         ItemVectorNormalizer normalizer,
                                                         @ModelSize int modelSize) {
        this.threshold = threshold;
        this.normalizer = normalizer;
        this.modelSize = modelSize;
    }

    /**
     * Creates and returns a NormalizingSimilarityMatrixAccumulator.
     *
     * @param itemUniverse The universe items the accumulator will accumulate.
     * @return a normalizing SimilarityMatrixAccumulator
     */
    public SimilarityMatrixAccumulator create(LongSortedSet itemUniverse) {
        return new NormalizingSimilarityMatrixAccumulator(itemUniverse, threshold, normalizer, modelSize);
    }

}
