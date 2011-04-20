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
