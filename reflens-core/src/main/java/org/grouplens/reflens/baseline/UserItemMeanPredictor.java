/**
 * 
 */
package org.grouplens.reflens.baseline;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.Collection;
import java.util.Map;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.util.CollectionUtils;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserItemMeanPredictor extends ItemMeanBaselinePredictor {

	protected UserItemMeanPredictor(double mean, Long2DoubleMap means) {
		super(mean, means);
	}
	
	double computeUserAverage(Map<Long,Double> ratings) {
		if (ratings.isEmpty()) return 0;
		
		Collection<Double> values = ratings.values();
		double total = 0;
		Long2DoubleMap fratings = CollectionUtils.getFastMap(ratings);
		
		for (Long2DoubleMap.Entry rating: CollectionUtils.fastIterable(fratings)) {
			double r = rating.getDoubleValue();
			long iid = rating.getLongKey();
			total += r - getItemMean(iid);
		}
		return total / values.size();
	}
	
	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(long, java.util.Map, java.util.Collection)
	 */
	@Override
	public Map<Long, Double> predict(long user, Map<Long, Double> ratings,
			Collection<Long> items) {
		double meanOffset = computeUserAverage(ratings);
		Long2DoubleMap map = new Long2DoubleOpenHashMap(items.size());
		LongCollection fitems = CollectionUtils.getFastCollection(items);
		LongIterator iter = fitems.iterator();
		while (iter.hasNext()) {
			long iid = iter.nextLong();
			map.put(iid, meanOffset + getItemMean(iid));
		}
		return map;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(long, java.util.Map, long)
	 */
	@Override
	public ScoredId predict(long user, Map<Long, Double> ratings, long item) {
		return new ScoredId(item, computeUserAverage(ratings) + getItemMean(item));
	}
	
	public static class Builder extends ItemMeanBaselinePredictor.Builder {
		@Override
		public RatingPredictor create(double globalMean, Long2DoubleMap itemMeans) {
			return new UserItemMeanPredictor(globalMean, itemMeans);
		}
	}

}
