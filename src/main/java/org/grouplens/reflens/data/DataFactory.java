package org.grouplens.reflens.data;

public interface DataFactory<U,I> {
	public Indexer<U> makeUserIndexer();
	public Indexer<I> makeItemIndexer();
	
	public RatingVector<U> makeItemRatingVector();
}
