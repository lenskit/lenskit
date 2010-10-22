package org.grouplens.reflens.data;

public interface Index<I> {

	public abstract int getIndex(I obj);

	public abstract I getObject(int idx);

	public abstract int getObjectCount();

}