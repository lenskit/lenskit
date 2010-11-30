package org.grouplens.reflens;

import org.grouplens.reflens.data.RatingDataSource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * {@link RecommenderService} that uses a {@link RecommenderBuilder} and data
 * source dataProvider to build a recommender engine.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Singleton
public class SimpleRecommenderService implements RecommenderService {
	private RecommendationEngine engine;
	private final RecommenderBuilder builder;
	private final Provider<RatingDataSource> dataProvider;
	
	@Inject
	public SimpleRecommenderService(RecommenderBuilder builder, Provider<RatingDataSource> dataProvider) {
		this.builder = builder;
		this.dataProvider = dataProvider;
	}

	/**
	 * Get the recommender engine.  If the recommender needs to be built, it
	 * will block all other threads asking for recommenders.
	 */
	@Override
	public synchronized RecommendationEngine getRecommender() {
		if (engine == null) {
			engine = builder.build(dataProvider.get());
		}
		return engine;
	}

}
