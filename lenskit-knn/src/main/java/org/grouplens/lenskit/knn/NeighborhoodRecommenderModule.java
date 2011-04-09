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
package org.grouplens.lenskit.knn;

import org.grouplens.lenskit.config.RecommenderModuleComponent;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.params.ItemSimilarity;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.knn.params.SimilarityDamper;
import org.grouplens.lenskit.knn.params.SimilarityThreshold;
import org.grouplens.lenskit.knn.params.UserSimilarity;

import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

/**
 * Modules providing parameters common to KNN recommenders.  It provides access
 * to all parameters in the {@link org.grouplens.lenskit.knn.params} package.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class NeighborhoodRecommenderModule extends RecommenderModuleComponent {
    private @NeighborhoodSize int neighborhoodSize;
    private @SimilarityDamper double similarityDamping;
    private @SimilarityThreshold double similarityThreshold;
    private @ItemSimilarity Class<? extends Similarity<? super SparseVector>> itemSimilarity;
    private @UserSimilarity Class<? extends Similarity<? super SparseVector>> userSimilarity;

    @Override
    protected void configure() {
        configureItemSimilarity();
        configureUserSimilarity();
    }

    /**
     * @return the neighborhoodSize
     */
    @Provides @NeighborhoodSize
    public int getNeighborhoodSize() {
        return neighborhoodSize;
    }

    /**
     * @param neighborhoodSize the neighborhoodSize to set
     */
    public void setNeighborhoodSize(int neighborhoodSize) {
        this.neighborhoodSize = neighborhoodSize;
    }

    /**
     * @return the similarityDamping
     */
    @Provides @SimilarityDamper
    public double getSimilarityDamping() {
        return similarityDamping;
    }

    /**
     * @param similarityDamping the similarityDamping to set
     */
    public void setSimilarityDamping(double similarityDamper) {
        this.similarityDamping = similarityDamper;
    }

    /**
     * Get the similarity threshold.
     * @return The similarity threshold.
     */
    @Provides @SimilarityThreshold
    public double getSimilarityThreshold() {
        return similarityThreshold;
    }
    
    /**
     * Set the similarity threshold.
     * @param thresh The new similarity threshold.
     */
    public void setSimilarityThreshold(double thresh) {
        similarityThreshold = thresh;
    }

    /**
     * @return the itemSimilarity
     */
    public Class<? extends Similarity<? super SparseVector>> getItemSimilarity() {
        return itemSimilarity;
    }

    /**
     * @todo make this fail-fast if a bad class is passed in.
     * @param itemSimilarity the itemSimilarity to set
     */
    public void setItemSimilarity(
            Class<? extends Similarity<? super SparseVector>> itemSimilarity) {
        this.itemSimilarity = itemSimilarity;
    }

    protected void configureItemSimilarity() {
        bind(new TypeLiteral<Similarity<? super SparseVector>>(){})
            .annotatedWith(ItemSimilarity.class)
            .to(itemSimilarity);
    }

    /**
     * @return the itemSimilarity
     */
    public Class<? extends Similarity<? super SparseVector>> getUserSimilarity() {
        return userSimilarity;
    }

    /**
     * @todo make this fail-fast if a bad class is passed in.
     * @param itemSimilarity the itemSimilarity to set
     */
    public void setUserSimilarity(
            Class<? extends Similarity<? super SparseVector>> userSimilarity) {
        this.userSimilarity = userSimilarity;
    }

    protected void configureUserSimilarity() {
        bind(new TypeLiteral<Similarity<? super SparseVector>>(){})
            .annotatedWith(UserSimilarity.class)
            .to(itemSimilarity);
    }

}
