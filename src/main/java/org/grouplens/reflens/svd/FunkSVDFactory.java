package org.grouplens.reflens.svd;

import java.util.Collection;

import org.grouplens.reflens.Recommender;
import org.grouplens.reflens.bench.BenchmarkRecommenderFactory;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.integer.IntDataFactory;

public class FunkSVDFactory implements BenchmarkRecommenderFactory {

	@Override
	public Recommender<Integer, Integer> buildRecommender(
			Collection<RatingVector<Integer, Integer>> ratings) {
		String numFeatures = System.getProperty("org.grouplens.reflens.svd.rank", "50");
		int nf = Integer.parseInt(numFeatures);
		return new FunkSVD<Integer,Integer>(new IntDataFactory(), ratings, nf, 0.001f);
	}

}
