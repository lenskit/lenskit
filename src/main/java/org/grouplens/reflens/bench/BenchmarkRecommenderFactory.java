package org.grouplens.reflens.bench;

import java.util.Collection;

import org.grouplens.reflens.Recommender;
import org.grouplens.reflens.data.RatingVector;

/**
 * Interface for recommender factories for the benchmarker to use.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface BenchmarkRecommenderFactory {
	/**
	 * Construct a new recommender engine trained on the provided ratings.
	 * @param ratings The set of initial ratings with which to seed the
	 * recommender.
	 * @return A new recommender engine.
	 */
	public Recommender<Integer,Integer> buildRecommender(Collection<RatingVector<Integer,Integer>> ratings);
}
