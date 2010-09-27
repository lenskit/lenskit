package org.grouplens.reflens.data;

import org.grouplens.reflens.data.generic.GenericRatingVectorFactory;

import com.google.inject.ImplementedBy;

@ImplementedBy(value=GenericRatingVectorFactory.class)
public interface RatingVectorFactory<E,T> {
	public RatingVector<E,T> make(E entity);
}
