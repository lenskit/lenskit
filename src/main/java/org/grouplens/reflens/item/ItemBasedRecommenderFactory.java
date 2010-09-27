package org.grouplens.reflens.item;

import java.util.Collection;

import org.grouplens.reflens.RecommenderFactory;
import org.grouplens.reflens.data.RatingVector;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ItemBasedRecommenderFactory<U,I> implements RecommenderFactory<U, I> {
	Provider<ItemBasedRecommender<U,I>> recProvider;
	
	@Inject
	ItemBasedRecommenderFactory(Provider<ItemBasedRecommender<U,I>> recProvider) {
		this.recProvider = recProvider;
	}
	
	@Override
	public ItemBasedRecommender<U,I> build(Collection<RatingVector<U,I>> data) {
		ItemBasedRecommender<U,I> rec = recProvider.get();
		rec.buildModel(data);
		return rec;
	}
}
