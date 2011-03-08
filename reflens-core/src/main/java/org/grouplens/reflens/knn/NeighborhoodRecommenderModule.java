package org.grouplens.reflens.knn;

import org.grouplens.reflens.RecommenderModuleComponent;
import org.grouplens.reflens.data.vector.SparseVector;
import org.grouplens.reflens.knn.params.ItemSimilarity;
import org.grouplens.reflens.knn.params.NeighborhoodSize;
import org.grouplens.reflens.knn.params.SimilarityDamper;
import org.grouplens.reflens.knn.params.UserSimilarity;

import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

/**
 * Modules providing parameters common to KNN recommenders.  It provides access
 * to all parameters in the {@link org.grouplens.reflens.knn.params} package.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class NeighborhoodRecommenderModule extends RecommenderModuleComponent {
	private @NeighborhoodSize int neighborhoodSize;
	private @SimilarityDamper double similarityDamping;
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
