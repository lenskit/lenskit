package org.grouplens.reflens.data;

import java.util.Collection;
import java.util.Map;

public class BasicUserRatingProfile<U, I> implements UserRatingProfile<U, I> {
	
	private U user;
	private Map<I, Float> ratings;

	public BasicUserRatingProfile(U user, Map<I,Float> ratings) {
		this.user = user;
		this.ratings = ratings;
	}
	
	public BasicUserRatingProfile(Map.Entry<? extends U, ? extends Map<I,Float>> entry) {
		this(entry.getKey(), entry.getValue());
	}

	@Override
	public float getRating(I item) {
		Float r = ratings.get(item);
		if (r == null)
			return Float.NaN;
		else
			return r;
	}

	@Override
	public Map<I,Float> getRatings() {
		return ratings;
	}

	@Override
	public U getUser() {
		return user;
	}

}
