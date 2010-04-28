package org.grouplens.reflens.data.generic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;

public class GenericRatingVector<T> implements RatingVector<T> {
	private Map<T, Float> ratings = new HashMap<T,Float>();
	Float average = null;

	@Override
	public void putRating(T obj, float rating) {
		average = null;
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

	@Override
	public float getAverage() {
		if (average == null) {
			float avg = 0.0f;
			for (Float v: ratings.values()) {
				avg += v;
			}
			average = avg / ratings.size();
		}
		return average;
	}
	
	@Override
	public GenericRatingVector<T> copy() {
		GenericRatingVector<T> v2 = new GenericRatingVector<T>();
		v2.ratings = new HashMap<T,Float>(ratings);
		v2.average = average;
		return v2;
	}
}