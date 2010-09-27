package org.grouplens.reflens.data;

import java.util.Map;

/**
 * Representation of rating vectors.  It consists of an <i>owner</i>, which has
 * ratings for a number of subjects.  The owner can be a user with ratings for
 * items, or it could be an item with ratings from users.  The rating vector
 * implies nothing about the direction of the ratings; the client code context
 * is responsible for that.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @param <S> The rating owner type.
 * @param <T> The type of rating subjects.
 */
public interface RatingVector<S, T> extends Iterable<ObjectValue<T>> {
	public S getOwner();
	public boolean containsObject(T object);
	public float getRating(T object);
	public Map<T,Float> getRatings();
	public void putRating(T obj, float rating);
	public float getAverage();
	public RatingVector<S, T> copy();
}
