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

import javax.annotation.Nullable;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RatingRecommender;
import org.grouplens.lenskit.RecommenderModule;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.NeighborhoodRecommenderModule;
import org.grouplens.lenskit.knn.OptimizableVectorSimilarity;
import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.knn.SimilarityMatrixBuilderFactory;
import org.grouplens.lenskit.knn.TruncatingSimilarityMatrixBuilder;
import org.grouplens.lenskit.knn.params.ItemSimilarity;
import org.grouplens.lenskit.params.BaselinePredictor;
import org.grouplens.lenskit.util.SymmetricBinaryFunction;

import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.throwingproviders.ThrowingProviderBinder;

/**
 * TODO Extract NeighborhoodRecommenderModule
 * TODO Document this class
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemRecommenderModule extends RecommenderModule {
    /**
     * Neighborhood recommender parameters.
     */
    public final NeighborhoodRecommenderModule knn;

    public ItemRecommenderModule() {
        knn = new NeighborhoodRecommenderModule();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        knn.setName(name);
    }

    @Override
    protected void configure() {
        super.configure();
        install(ThrowingProviderBinder.forModule(this));
        install(knn);
        configureSimilarityMatrix();
        bind(RatingPredictor.class).to(ItemItemRatingPredictor.class);
        bind(RatingRecommender.class).to(ItemItemRatingRecommender.class);
    }

    /**
     *
     */
    protected void configureSimilarityMatrix() {
        bind(SimilarityMatrixBuilderFactory.class).toProvider(
                FactoryProvider.newFactory(SimilarityMatrixBuilderFactory.class,
                        TruncatingSimilarityMatrixBuilder.class));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Provides
    protected SimilarityMatrixBuildStrategy buildStrategy(
            SimilarityMatrixBuilderFactory matrixFactory,
            @ItemSimilarity Similarity<? super SparseVector> similarity) {
        if (similarity instanceof OptimizableVectorSimilarity) {
            if (similarity instanceof SymmetricBinaryFunction)
                return new OptimizedSymmetricSimilarityMatrixBuildStrategy(matrixFactory,
                        (OptimizableVectorSimilarity) similarity);
            else
                return new OptimizedSimilarityMatrixBuildStrategy(matrixFactory,
                        (OptimizableVectorSimilarity) similarity);
        } else {
            if (similarity instanceof SymmetricBinaryFunction)
                return new SymmetricSimilarityMatrixBuildStrategy(matrixFactory, similarity);
            else
                return new SimpleSimilarityMatrixBuildStrategy(matrixFactory, similarity);
        }
    }
    
    @Provides
    public ItemItemModel provideModel(ItemItemRecommenderBuilder builder,
            Provider<RatingBuildContext> context, @Nullable @BaselinePredictor RatingPredictor baseline) {
        return builder.build(context.get(), baseline);
    }
}
