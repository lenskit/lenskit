package org.grouplens.reflens.data;

import java.util.Map;

public interface RatingVector<I> extends Iterable<ObjectValue<I>> {
	public boolean containsObject(I object);
	public float getRating(I object);
	public Map<I,Float> getRatings();
	public void addRating(I obj, float rating);
}
