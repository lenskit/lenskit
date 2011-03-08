package org.grouplens.reflens.knn.user;

import org.grouplens.reflens.RecommenderModule;
import org.grouplens.reflens.RecommenderService;
import org.grouplens.reflens.knn.NeighborhoodRecommenderModule;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserRecommenderModule extends RecommenderModule {
	public final NeighborhoodRecommenderModule knn = new NeighborhoodRecommenderModule();
	
	@Override
	protected void configure() {
		super.configure();
		install(knn);
		bind(RecommenderService.class).to(SimpleUserUserRecommenderService.class);
	}
}