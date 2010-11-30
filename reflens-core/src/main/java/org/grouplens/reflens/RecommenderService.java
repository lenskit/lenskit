package org.grouplens.reflens;

/**
 * Service providing {@link RecommendationEngine}s.  This service is to be used
 * by the application to acquire the recommender; it is responsible for building
 * or otherwise loading it.
 * 
 * This interface is thread-safe; it is safe for multiple threads to 
 * simultaneously request a recommender, although they may need to coordinate
 * around its use.
 * 
 * TODO Define and document thread safety of recommendation engines.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface RecommenderService {
	RecommendationEngine getRecommender();
}
