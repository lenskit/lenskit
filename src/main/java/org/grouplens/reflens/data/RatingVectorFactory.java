package org.grouplens.reflens.data;

public interface RatingVectorFactory<E,T> {
	public RatingVector<E,T> make(E entity);
}
