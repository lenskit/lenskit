package org.grouplens.reflens;

import java.util.Collection;

import org.grouplens.reflens.data.RatingVector;

/**
 * Interface for recommender factories for the benchmarker to use.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface RecommenderFactory<U,I> {
	/**
	 * Construct a new recommender engine trained on the provided ratings.
	 * @param ratings The set of initial ratings with which to seed the
	 * recommender.
	 * @return A new recommender engine.
	 */
	public Recommender<U,I> build(Collection<RatingVector<U,I>> ratings);
}
