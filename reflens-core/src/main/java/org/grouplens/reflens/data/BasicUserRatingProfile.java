package org.grouplens.reflens.data;

import java.util.Map;

public class BasicUserRatingProfile<U, I> implements UserRatingProfile<U, I> {
	
	private U user;
	private Map<I, Double> ratings;

	public BasicUserRatingProfile(U user, Map<I,Double> ratings) {
		this.user = user;
		this.ratings = ratings;
	}
	
	public BasicUserRatingProfile(Map.Entry<? extends U, ? extends Map<I,Double>> entry) {
		this(entry.getKey(), entry.getValue());
	}

	@Override
	public double getRating(I item) {
		Double r = ratings.get(item);
		if (r == null)
			return Double.NaN;
		else
			return r;
	}

	@Override
	public Map<I,Double> getRatings() {
		return ratings;
	}

	@Override
	public U getUser() {
		return user;
	}

}
