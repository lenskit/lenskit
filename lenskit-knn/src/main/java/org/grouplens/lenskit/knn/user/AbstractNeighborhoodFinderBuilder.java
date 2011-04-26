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
package org.grouplens.lenskit.knn.user;

import org.grouplens.lenskit.AbstractRecommenderComponentBuilder;
import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.PearsonCorrelation;
import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.norm.IdentityUserRatingVectorNormalizer;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractNeighborhoodFinderBuilder<T extends NeighborhoodFinder>
        extends AbstractRecommenderComponentBuilder<T> {

    protected int neighborhoodSize;
    protected Similarity<? super SparseVector> similarity;
    protected RecommenderComponentBuilder<? extends UserRatingVectorNormalizer> normalizerBuilder;

    protected AbstractNeighborhoodFinderBuilder() {
        neighborhoodSize = 100;
        similarity = new PearsonCorrelation();
        normalizerBuilder = new IdentityUserRatingVectorNormalizer.Builder();
    }

    public int getNeighborhoodSize() {
        return neighborhoodSize;
    }

    public void setNeighborhoodSize(int neighborhood) {
        neighborhoodSize = neighborhood;
    }

    public Similarity<? super SparseVector> getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Similarity<? super SparseVector> similarity) {
        this.similarity = similarity;
    }

    /**
     * Get the normalizer builder.
     * @return The normalizer builder.
     */
    public RecommenderComponentBuilder<? extends UserRatingVectorNormalizer> getNormalizer() {
        return normalizerBuilder;
    }

    /**
     * Set the normalizer builder.
     * @param normalizerBuilder The normalizer builder instance.
     */
    public void setNormalizer(RecommenderComponentBuilder<? extends UserRatingVectorNormalizer> normalizerBuilder) {
        this.normalizerBuilder = normalizerBuilder;
    }
}
