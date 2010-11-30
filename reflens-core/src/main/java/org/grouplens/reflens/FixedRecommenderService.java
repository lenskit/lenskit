package org.grouplens.reflens;

import com.google.inject.Inject;

/**
 * {@link RecommenderService} that returns a recommender engine passed as its
 * constructor.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class FixedRecommenderService implements RecommenderService {
	private final RecommendationEngine engine;
	
	@Inject
	public FixedRecommenderService(RecommendationEngine engine) {
		this.engine = engine;
	}
	
	@Override
	public RecommendationEngine getRecommender() {
		return engine;
	}

}
