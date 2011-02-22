package org.grouplens.reflens;

import com.google.inject.ImplementedBy;
import com.google.inject.throwingproviders.CheckedProvider;

/**
 * Provider of recommender services.
 * 
 * <p>This provider allows client code to access recommender services.  If the
 * recommender is not available for some reason, the provider will throw an
 * exception.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ImplementedBy(SimpleRecommenderServiceProvider.class)
public interface RecommenderServiceProvider extends CheckedProvider<RecommenderService> {
	/**
	 * Get (or build) the recommender service.
	 * @return A recommender service. This can be a new object, an object from
	 * a cache, or a recommender from somewhere else.  The returned object can
	 * be freely used without locking; if the particular implementation is not
	 * thread-safe or is otherwise scope-limited, it is the responsibility of the
	 * provider and/or bindings to take care of that and return a usable object.
	 * @throws RecommenderNotAvailableException if the recommender cannot be
	 * retrieved or built.
	 */
	RecommenderService get() throws RecommenderNotAvailableException;
}
