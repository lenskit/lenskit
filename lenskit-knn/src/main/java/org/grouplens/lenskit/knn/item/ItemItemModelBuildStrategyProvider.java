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
import org.grouplens.lenskit.knn.OptimizableVectorSimilarity;
import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.knn.SimilarityMatrixBuilderFactory;
import org.grouplens.lenskit.knn.params.ItemSimilarity;
import org.grouplens.lenskit.util.SymmetricBinaryFunction;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Provide an item model building strategy based on the type of the similarity
 * function.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemItemModelBuildStrategyProvider implements
        Provider<ItemItemModelBuildStrategy> {
    
    private SimilarityMatrixBuilderFactory matrixFactory;
    private Similarity<? super SparseVector> similarity;
    
    @Inject
    public ItemItemModelBuildStrategyProvider(SimilarityMatrixBuilderFactory mfact,
            @ItemSimilarity Similarity<? super SparseVector> sim) {
        matrixFactory = mfact;
        similarity = sim;
    }

    /* (non-Javadoc)
     * @see com.google.inject.Provider#get()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ItemItemModelBuildStrategy get() {
        if (similarity instanceof OptimizableVectorSimilarity) {
            if (similarity instanceof SymmetricBinaryFunction)
                return new SparseSymmetricModelBuildStrategy(matrixFactory,
                        (OptimizableVectorSimilarity) similarity);
            else
                return new SparseModelBuildStrategy(matrixFactory,
                        (OptimizableVectorSimilarity) similarity);
        } else {
            if (similarity instanceof SymmetricBinaryFunction)
                return new SymmetricModelBuildStrategy(matrixFactory, similarity);
            else
                return new SimpleModelBuildStrategy(matrixFactory, similarity);
        }
    }

}
