package org.grouplens.reflens.data;

import java.util.Map;

public interface DataFactory<U,I> {
	public Indexer<U> makeUserIndexer();
	public Indexer<I> makeItemIndexer();
	
	public Map<I,Float> makeItemFloatMap();
	
	public RatingVector<I,U> makeItemRatingVector(I item);
}
