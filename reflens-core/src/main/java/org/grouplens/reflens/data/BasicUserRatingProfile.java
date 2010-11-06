package org.grouplens.reflens.data;

import java.util.Map;

public class BasicUserRatingProfile implements UserRatingProfile {
	
	private long user;
	private Map<Long, Double> ratings;

	public BasicUserRatingProfile(long user, Map<Long,Double> ratings) {
		this.user = user;
		this.ratings = ratings;
	}
	
	public BasicUserRatingProfile(Map.Entry<Long, ? extends Map<Long,Double>> entry) {
		this(entry.getKey(), entry.getValue());
	}

	@Override
	public double getRating(long item) {
		Double r = ratings.get(item);
		if (r == null)
			return Double.NaN;
		else
			return r;
	}

	@Override
	public Map<Long,Double> getRatings() {
		return ratings;
	}

	@Override
	public long getUser() {
		return user;
	}

}
