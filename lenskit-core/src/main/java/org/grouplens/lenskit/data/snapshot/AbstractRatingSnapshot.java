package org.grouplens.lenskit.data.snapshot;

import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.data.vector.VectorCache;

public abstract class AbstractRatingSnapshot implements RatingSnapshot {

	protected VectorCache cache;
	
	public AbstractRatingSnapshot() {
		cache = new VectorCache();
	}
	
	@Override
	public SparseVector userRatingVector(long userId) {
		SparseVector data = cache.get(userId);
		if (data != null) return data;
		else {
			data = Ratings.userRatingVector(this.getUserRatings(userId));
			cache.put(userId, data);
			return data;
		}
	}	
}
