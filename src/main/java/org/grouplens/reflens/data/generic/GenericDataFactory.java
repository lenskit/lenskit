package org.grouplens.reflens.data.generic;

import java.util.HashMap;
import java.util.Map;

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
	public RatingVector<I,U> makeItemRatingVector(I item) {
		return new GenericRatingVector<I,U>(item);
	}

	@Override
	public Map<I, Float> makeItemFloatMap() {
		return new HashMap<I,Float>();
	}

}
