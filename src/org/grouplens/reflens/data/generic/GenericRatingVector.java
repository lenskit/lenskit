package org.grouplens.reflens.data.generic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;

public class GenericRatingVector<T> implements RatingVector<T> {
	private Map<T, Float> ratings = new HashMap<T,Float>();

	@Override
	public void addRating(T obj, float rating) {
		ratings.put(obj, rating);
	}

	@Override
	public boolean containsObject(T object) {
		return ratings.containsKey(object);
	}

	@Override
	public float getRating(T object) {
		return ratings.get(object);
	}

	@Override
	public Map<T, Float> getRatings() {
		return ratings;
	}

	@Override
	public Iterator<ObjectValue<T>> iterator() {
		return ObjectValue.wrap(ratings.entrySet()).iterator();
	}

}