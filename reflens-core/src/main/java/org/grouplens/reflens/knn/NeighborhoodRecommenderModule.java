package org.grouplens.reflens.knn;

import org.grouplens.reflens.RecommenderModule;
import org.grouplens.reflens.knn.params.NeighborhoodSize;
import org.grouplens.reflens.knn.params.SimilarityDamper;

import com.google.inject.Provides;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class NeighborhoodRecommenderModule extends RecommenderModule {
	private @NeighborhoodSize int neighborhoodSize;
	private @SimilarityDamper double similarityDamping;
	
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
}
