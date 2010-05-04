package org.grouplens.reflens.data.generic;

import java.util.Iterator;
import java.util.Map;

import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;

public class GenericRatingVector<S, T> implements RatingVector<S, T> {
	protected MapFactory<T, Float> factory;
	protected Map<T, Float> ratings;
	protected Float average = null;
	protected final S owner;
	
	public GenericRatingVector() {
		this(null);
	}
	
	public GenericRatingVector(S owner) {
		this(new GenericMapFactory<T>(), owner, null);
	}
	
	public GenericRatingVector(S owner, Map<T,Float> ratings) {
		this(new GenericMapFactory<T>(), owner, ratings);
	}
	
	protected GenericRatingVector(MapFactory<T,Float> factory, S owner, Map<T,Float> ratings) {
		this.owner = owner;
		this.factory = factory;
		if (ratings == null) {
			this.ratings = factory.create();
		} else {
			this.ratings = factory.copy(ratings);
		}
	}

	@Override
	public S getOwner() {
		return owner;
	}
	
	@Override
	public void putRating(T obj, float rating) {
		average = null;
		ratings.put(obj, rating);
	}

	@Override
	public boolean containsObject(T key) {
		return ratings.containsKey(key);
	}

	@Override
	public float getRating(T key) {
		return ratings.get(key);
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
	public GenericRatingVector<S, T> copy() {
		GenericRatingVector<S, T> v2 = new GenericRatingVector<S,T>(factory, owner, ratings);
		v2.average = average;
		return v2;
	}
}