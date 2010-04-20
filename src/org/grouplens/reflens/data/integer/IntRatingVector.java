package org.grouplens.reflens.data.integer;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;

import bak.pcj.adapter.IntKeyFloatMapToMapAdapter;
import bak.pcj.map.IntKeyFloatMap;
import bak.pcj.map.IntKeyFloatOpenHashMap;

public class IntRatingVector implements RatingVector<Integer> {
	protected IntKeyFloatMap data;
	private Map<Integer,Float> wrapper;
	
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
	public void addRating(Integer obj, float rating) {
		data.put(obj, rating);
	}
}
