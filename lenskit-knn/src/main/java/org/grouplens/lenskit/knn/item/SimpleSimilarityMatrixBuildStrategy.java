/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.knn.item;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.knn.SimilarityMatrix;
import org.grouplens.lenskit.knn.SimilarityMatrixBuilder;
import org.grouplens.lenskit.knn.SimilarityMatrixBuilderFactory;
import org.grouplens.lenskit.knn.item.ItemItemModelBuilder.BuildState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Similarity matrix strategy that assumes nothing about the similarity function.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class SimpleSimilarityMatrixBuildStrategy implements
        SimilarityMatrixBuildStrategy {
    private final static Logger logger = LoggerFactory.getLogger(SimpleSimilarityMatrixBuildStrategy.class);

    private final SimilarityMatrixBuilderFactory matrixFactory;
    private final Similarity<? super SparseVector> similarityFunction;

    SimpleSimilarityMatrixBuildStrategy(SimilarityMatrixBuilderFactory matrixFactory, Similarity<? super SparseVector> similarity) {
        this.matrixFactory = matrixFactory;
        this.similarityFunction = similarity;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.knn.SimilarityMatrixBuildStrategy#buildMatrix(org.grouplens.lenskit.knn.ItemItemRecommenderBuilder.BuildState)
     */
    @Override
    public SimilarityMatrix buildMatrix(BuildState state) {
        final int nitems = state.itemCount;
        logger.debug("Building matrix with {} rows");
        SimilarityMatrixBuilder builder = matrixFactory.create(state.itemCount);
        for (int i = 0; i < nitems; i++) {
            for (int j = 0; j < nitems; j++) {
                if (i == j) continue;
                double sim = similarityFunction.similarity(state.itemRatings.get(i), state.itemRatings.get(j));
                builder.put(i, j, sim);
            }
        }
        return builder.build();
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.knn.SimilarityMatrixBuildStrategy#needsUserItemSets()
     */
    @Override
    public boolean needsUserItemSets() {
        return false;
    }

}
