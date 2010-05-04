package org.grouplens.reflens.data;

import java.util.Map;

public interface RatingVector<S, T> extends Iterable<ObjectValue<T>> {
	public S getOwner();
	public boolean containsObject(T object);
	public float getRating(T object);
	public Map<T,Float> getRatings();
	public void putRating(T obj, float rating);
	public float getAverage();
	public RatingVector<S, T> copy();
}
