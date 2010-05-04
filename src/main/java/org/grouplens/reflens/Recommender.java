package org.grouplens.reflens;

import java.util.List;
import java.util.Set;

import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;

public interface Recommender<U,I> {
	public List<ObjectValue<I>> recommend(RatingVector<U,I> user);
	public List<ObjectValue<I>> recommend(Set<I> basket);
	public ObjectValue<I> predict(RatingVector<U,I> user, I item);
}