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
	private final RecommenderEngine engine;
	
	@Inject
	public FixedRecommenderService(RecommenderEngine engine) {
		this.engine = engine;
	}
	
	@Override
	public RecommenderEngine getRecommender() {
		return engine;
	}

}
