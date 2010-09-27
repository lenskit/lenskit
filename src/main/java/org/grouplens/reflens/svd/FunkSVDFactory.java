package org.grouplens.reflens.svd;

import java.util.Collection;

import org.grouplens.reflens.Recommender;
import org.grouplens.reflens.RecommenderFactory;
import org.grouplens.reflens.data.RatingVector;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class FunkSVDFactory<U,I> implements RecommenderFactory<U,I> {
	
	private Provider<FunkSVD<U,I>> svdProvider;
	@Inject
	public FunkSVDFactory(Provider<FunkSVD<U,I>> provider) {
		svdProvider = provider;
	}

	@Override
	public Recommender<U, I> build(
			Collection<RatingVector<U, I>> ratings) {
		FunkSVD<U,I> rec = svdProvider.get();
		rec.build(ratings);
		return rec;
	}

}
