package org.grouplens.reflens.data.integer;

import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.UserHistory;

import bak.pcj.FloatIterator;
import bak.pcj.map.IntKeyFloatMap;

public class IntUserHistory extends IntRatingVector implements UserHistory<Integer,Integer> {
	int user;
	
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
	
	public IntUserHistory copy() {
		return new IntUserHistory(user, data);
	}
}
