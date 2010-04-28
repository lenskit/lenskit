package org.grouplens.reflens.data;

import java.util.Map;

public interface RatingVector<I> extends Iterable<ObjectValue<I>> {
	public boolean containsObject(I object);
	public float getRating(I object);
	public Map<I,Float> getRatings();
	public void putRating(I obj, float rating);
	public float getAverage();
	public RatingVector<I> copy();
}
