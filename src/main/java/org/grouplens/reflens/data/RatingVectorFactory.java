package org.grouplens.reflens.data;

import org.grouplens.reflens.data.generic.GenericRatingVectorFactory;

import com.google.inject.ImplementedBy;

@ImplementedBy(GenericRatingVectorFactory.class)
public interface RatingVectorFactory<S,T> {
	public RatingVector<S,T> make(S owner);
}
