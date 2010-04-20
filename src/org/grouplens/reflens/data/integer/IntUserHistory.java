package org.grouplens.reflens.data.integer;

import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.UserHistory;

import bak.pcj.FloatIterator;
import bak.pcj.map.IntKeyFloatMap;

public class IntUserHistory extends IntRatingVector implements UserHistory<Integer,Integer> {
	int user;
	Float averageRating;
	
	public IntUserHistory(int user, IntKeyFloatMap ratings) {
		super(ratings);
		this.user = user;
	}
	public IntUserHistory(int user, RatingVector<Integer> ratings) {
		super(ratings.getRatings());
		this.user = user;
	}
	
	@Override
	public Integer getUser() {
		return user;
	}
	
	@Override
	public float getAverageRating() {
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
	public void addRating(Integer obj, float rating) {
		super.addRating(obj, rating);
		averageRating = null;
	}

}
