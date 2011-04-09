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

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RatingRecommender;
import org.grouplens.lenskit.config.RecommenderModule;
import org.grouplens.lenskit.knn.NeighborhoodRecommenderModule;
import org.grouplens.lenskit.knn.SimilarityMatrixBuilderFactory;
import org.grouplens.lenskit.knn.TruncatingSimilarityMatrixBuilder;

import com.google.inject.ProvidedBy;
import com.google.inject.assistedinject.FactoryProvider;

/**
 * Module for configuring item-item CF recommenders.
 * 
 * <p>This module provides the base code for configuring item-item recommenders.
 * Its {@link #configure()} method binds all parameters (including core and KNN
 * parameters as defined by {@link NeighborhoodRecommenderModule}).  It then
 * sets up the similarity matrix binding using the {@link #configureSimilarityMatrix()}
 * method and finally binds the {@link RatingPredictor} and {@link RatingRecommender}
 * classes to their respective implementations in this package.
 * 
 * <p>To modify the recommender configuration, there are a few of primary
 * extension points:
 * 
 * <ul>
 * <li>Override {@link #configureSimilarityMatrix()} to change what similarity
 * matrix builder implementation is used.
 * <li>Override {@link #configure()} and, after calling the superclass method,
 * bind subclasses of {@link ItemItemRatingPredictor} or {@link ItemItemRatingRecommender}
 * to change predictor or recommender logic.
 * <li>Bind an alternative implementation or provider for {@link ItemItemModel},
 * overriding its {@link ProvidedBy} annotation.
 * </ul>
 * 
 * <p>Of course, other extensions are possible; consult the module source code
 * and the Guice documentation for details.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemItemCFModule extends RecommenderModule {
    /**
     * Neighborhood recommender parameters.
     */
    public final NeighborhoodRecommenderModule knn;

    public ItemItemCFModule() {
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
}
