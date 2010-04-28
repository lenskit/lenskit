package org.grouplens.reflens.item;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;

import java.util.List;

import org.grouplens.reflens.Normalization;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.data.DataFactory;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.UserHistory;
import org.grouplens.reflens.data.generic.GenericDataFactory;

public class ItemBasedRecommenderFactory<U,I> {
	private int similarityListSize = 0;
	private Normalization<UserHistory<U,I>> ratingNormalizer = null;
	private Similarity<RatingVector<U>> itemSimilarity = null;
	private Normalization<Int2FloatMap> itemSimilarityNormalizer = null;
	private DataFactory<U,I> dataFactory = new GenericDataFactory<U,I>();
	
	public ItemBasedRecommender<U,I> create(List<UserHistory<U,I>> data) {
		ItemBasedRecommender<U,I> rec = new ItemBasedRecommender<U,I>(
				similarityListSize,
				ratingNormalizer, itemSimilarity, itemSimilarityNormalizer,
				dataFactory);
		rec.buildModel(data);
		return rec;
	}

	/**
	 * @return the similarityListSize
	 */
	public int getSimilarityListSize() {
		return similarityListSize;
	}

	/**
	 * @param similarityListSize the similarityListSize to set
	 */
	public void setSimilarityListSize(int similarityListSize) {
		this.similarityListSize = similarityListSize;
	}

	/**
	 * @return the ratingNormalizer
	 */
	public Normalization<UserHistory<U, I>> getRatingNormalizer() {
		return ratingNormalizer;
	}

	/**
	 * @param ratingNormalizer the ratingNormalizer to set
	 */
	public void setRatingNormalizer(
			Normalization<UserHistory<U, I>> ratingNormalizer) {
		this.ratingNormalizer = ratingNormalizer;
	}

	/**
	 * @return the itemSimilarity
	 */
	public Similarity<RatingVector<U>> getItemSimilarity() {
		return itemSimilarity;
	}

	/**
	 * @param itemSimilarity the itemSimilarity to set
	 */
	public void setItemSimilarity(Similarity<RatingVector<U>> itemSimilarity) {
		this.itemSimilarity = itemSimilarity;
	}

	/**
	 * @return the itemSimilarityNormalizer
	 */
	public Normalization<Int2FloatMap> getItemSimilarityNormalizer() {
		return itemSimilarityNormalizer;
	}

	/**
	 * @param itemSimilarityNormalizer the itemSimilarityNormalizer to set
	 */
	public void setItemSimilarityNormalizer(
			Normalization<Int2FloatMap> itemSimilarityNormalizer) {
		this.itemSimilarityNormalizer = itemSimilarityNormalizer;
	}

	/**
	 * @return the dataFactory
	 */
	public DataFactory<U, I> getDataFactory() {
		return dataFactory;
	}

	/**
	 * @param dataFactory the dataFactory to set
	 */
	public void setDataFactory(DataFactory<U, I> dataFactory) {
		this.dataFactory = dataFactory;
	}
}
