package org.grouplens.reflens.data;

public interface Indexer<I> {
	public int getIndex(I obj);
	public int getIndex(I obj, boolean insert);
	public I getObject(int idx);
	public int getObjectCount();
}
