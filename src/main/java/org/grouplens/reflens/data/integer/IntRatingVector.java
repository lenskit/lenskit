package org.grouplens.reflens.data.integer;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;

import bak.pcj.FloatIterator;
import bak.pcj.adapter.IntKeyFloatMapToMapAdapter;
import bak.pcj.map.IntKeyFloatMap;
import bak.pcj.map.IntKeyFloatOpenHashMap;

public class IntRatingVector implements RatingVector<Integer> {
	protected IntKeyFloatMap data;
	private Map<Integer,Float> wrapper;
	protected Float averageRating = null;
	
	@SuppressWarnings("unchecked")
	public IntRatingVector() {
		data = new IntKeyFloatOpenHashMap();
		wrapper = new IntKeyFloatMapToMapAdapter(data);
	}
	@SuppressWarnings("unchecked")
	public IntRatingVector(IntKeyFloatMap ratings) {
		data = new IntKeyFloatOpenHashMap(ratings);
		wrapper = new IntKeyFloatMapToMapAdapter(data);
	}
	@SuppressWarnings("unchecked")
	public IntRatingVector(Map<Integer,Float> ratings) {
		data = new IntKeyFloatOpenHashMap();
		for (Map.Entry<Integer, Float> entry: ratings.entrySet()) {
			data.put(entry.getKey(), entry.getValue());
		}
		wrapper = new IntKeyFloatMapToMapAdapter(data);
	}
	
	@Override
	public boolean containsObject(Integer object) {
		return data.containsKey(object);
	}

	@Override
	public float getRating(Integer object) {
		return data.get(object);
	}

	@Override
	public Iterator<ObjectValue<Integer>> iterator() {
		return ObjectValue.wrap(wrapper.entrySet()).iterator();
	}
	@Override
	public Map<Integer, Float> getRatings() {
		return Collections.unmodifiableMap(wrapper);
	}
	
	@Override
	public void putRating(Integer obj, float rating) {
		averageRating = null;
		data.put(obj, rating);
	}
	
	@Override
	public float getAverage() {
		if (averageRating == null) {
			float sum = 0;
			FloatIterator iter = data.values().iterator();
			while (iter.hasNext()) {
				sum += iter.next();
			}
			averageRating = sum / data.size();
		}
		return averageRating;
	}
	
	@Override
	public IntRatingVector copy() {
		IntRatingVector v2 = new IntRatingVector(data);
		v2.averageRating = averageRating;
		return v2;
	}
}
