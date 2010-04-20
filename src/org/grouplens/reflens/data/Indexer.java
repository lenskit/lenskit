package org.grouplens.reflens.data;

public interface Indexer<I> {
	public int getIndex(I obj);
	public I getObject(int idx);
}
