package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Collection;
import java.util.Map;

public class BasicUserRatingProfile implements UserRatingProfile {
	
	private long user;
	private Long2ObjectMap<Rating> ratings;

	public BasicUserRatingProfile(long user, Collection<Rating> ratings) {
		this.user = user;
		this.ratings = new Long2ObjectOpenHashMap<Rating>();
		for (Rating r: ratings) {
			this.ratings.put(r.getItemId(), r);
		}
	}
	
	public BasicUserRatingProfile(Map.Entry<Long, ? extends Collection<Rating>> entry) {
		this(entry.getKey(), entry.getValue());
	}

	@Override
	public double getRating(long item) {
		Rating r = ratings.get(item);
		if (r == null)
			return Double.NaN;
		else
			return r.getRating();
	}

	@Override
	public Collection<Rating> getRatings() {
		return ratings.values();
	}

	@Override
	public long getUser() {
		return user;
	}

}
