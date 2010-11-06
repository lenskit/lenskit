package org.grouplens.reflens.data;

public interface Index {
	public abstract int getIndex(long obj);
	public abstract long getId(int idx);
	public abstract int getObjectCount();
}