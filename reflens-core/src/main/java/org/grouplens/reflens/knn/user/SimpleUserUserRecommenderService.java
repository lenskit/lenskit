package org.grouplens.reflens.knn.user;

import org.grouplens.reflens.BasketRecommender;
import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingRecommender;
import org.grouplens.reflens.RecommenderService;

import com.google.inject.Inject;

/**
 * Simple user-user recommender that does a brute-force search over the
 * data source for every recommendation.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SimpleUserUserRecommenderService implements RecommenderService {
	private final SimpleUserUserRatingRecommender recommender;
	
	@Inject
	SimpleUserUserRecommenderService(SimpleUserUserRatingRecommender recommender) {
		this.recommender = recommender;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RecommenderService#getRatingRecommender()
	 */
	@Override
	public RatingRecommender getRatingRecommender() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RecommenderService#getRatingPredictor()
	 */
	@Override
	public RatingPredictor getRatingPredictor() {
		return recommender;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RecommenderService#getBasketRecommender()
	 */
	@Override
	public BasketRecommender getBasketRecommender() {
		// TODO Auto-generated method stub
		return null;
	}

}
