package org.grouplens.reflens.data.generic;

import org.grouplens.reflens.data.DataFactory;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.RatingVector;

public class GenericDataFactory<U,I> implements DataFactory<U,I> {

	@Override
	public Indexer<I> makeItemIndexer() {
		return new GenericIndexer<I>();
	}

	@Override
	public Indexer<U> makeUserIndexer() {
		return new GenericIndexer<U>();
	}

	@Override
	public RatingVector<U> makeItemRatingVector() {
		return new GenericRatingVector<U>();
	}

}
