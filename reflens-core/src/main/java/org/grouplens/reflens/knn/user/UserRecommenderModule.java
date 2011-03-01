package org.grouplens.reflens.knn.user;

import org.grouplens.reflens.RecommenderService;
import org.grouplens.reflens.data.vector.SparseVector;
import org.grouplens.reflens.knn.NeighborhoodRecommenderModule;
import org.grouplens.reflens.knn.Similarity;
import org.grouplens.reflens.knn.params.UserSimilarity;

import com.google.inject.TypeLiteral;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserRecommenderModule extends NeighborhoodRecommenderModule {
	private @UserSimilarity Class<? extends Similarity<? super SparseVector>> userSimilarity;
	
	@Override
	protected void configure() {
		bind(new TypeLiteral<Similarity<? super SparseVector>>() {})
			.annotatedWith(UserSimilarity.class)
			.to(userSimilarity);
		bind(RecommenderService.class).to(SimpleUserUserRecommenderService.class);
	}

	/**
	 * @return the userSimilarity
	 */
	public Class<? extends Similarity<? super SparseVector>> getUserSimilarity() {
		return userSimilarity;
	}

	/**
	 * @param userSimilarity the userSimilarity to set
	 */
	public void setUserSimilarity(
			Class<? extends Similarity<? super SparseVector>> userSimilarity) {
		this.userSimilarity = userSimilarity;
	}
}