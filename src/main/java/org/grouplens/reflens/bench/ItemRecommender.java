package org.grouplens.reflens.bench;

import java.util.Collection;

import org.grouplens.reflens.Recommender;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.item.CosineSimilarity;
import org.grouplens.reflens.item.ItemBasedRecommenderFactory;
import org.grouplens.reflens.item.MeanNormalization;

public class ItemRecommender implements RecommenderFactory {
	private ItemBasedRecommenderFactory<Integer,Integer> factory;
	
	public ItemRecommender() {
		factory = new ItemBasedRecommenderFactory<Integer,Integer>();
		factory.setItemSimilarity(new CosineSimilarity<Integer, RatingVector<Integer,Integer>>());
		factory.setRatingNormalizer(new MeanNormalization<Integer,Integer>());
	}

	@Override
	public Recommender<Integer, Integer> buildRecommender(
			Collection<RatingVector<Integer, Integer>> ratings) {
		return factory.create(ratings);
	}
}
