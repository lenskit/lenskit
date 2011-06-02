package org.grouplens.lenskit.data.snapshot;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.vector.SparseVector;

public abstract class AbstractRatingSnapshot implements RatingSnapshot {

	protected volatile Long2ObjectMap<SparseVector> cache;
	
	public AbstractRatingSnapshot() {
		cache = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<SparseVector>());
	}
	
	@Override
	public SparseVector userRatingVector(long userId) {
		SparseVector data = cache.get(userId);
		if (data != null) {
			return data;
		}
		else {
			data = Ratings.userRatingVector(this.getUserRatings(userId));
			cache.put(userId, data);
			return data;
		}
	}
	
	@OverridingMethodsMustInvokeSuper
	public void close() {
		cache = null;
	}
}
