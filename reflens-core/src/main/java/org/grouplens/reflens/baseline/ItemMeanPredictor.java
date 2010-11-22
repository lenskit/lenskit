/**
 * 
 */
package org.grouplens.reflens.baseline;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.Collection;
import java.util.Map;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemMeanPredictor implements RatingPredictor {
	private final Long2DoubleMap itemAverages;
	private final double globalMean;
	
	protected ItemMeanPredictor(double mean, Long2DoubleMap means) {
		globalMean = mean;
		itemAverages = means;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(long, java.util.Map, java.util.Collection)
	 */
	@Override
	public Map<Long, Double> predict(long user, Map<Long, Double> ratings,
			Collection<Long> items) {
		Long2DoubleMap predictions = new Long2DoubleOpenHashMap(items.size());
		LongCollection fitems = CollectionUtils.getFastCollection(items);
		LongIterator iter = fitems.iterator();
		while (iter.hasNext()) {
			long iid = iter.nextLong();
			predictions.put(iid, getItemMean(iid));
		}
		return predictions;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(long, java.util.Map, long)
	 */
	@Override
	public ScoredId predict(long user, Map<Long, Double> ratings, long item) {
		return new ScoredId(item, getItemMean(item));
	}
	
	protected double getItemMean(long id) {
		return globalMean + itemAverages.get(id);
	}

	public static class Builder implements RatingPredictorBuilder {
		private static Logger logger = LoggerFactory.getLogger(Builder.class);

		@Override
		public RatingPredictor build(RatingDataSource data) {
			double total = 0.0;
			int count = 0;
			Long2DoubleMap itemTotals = new Long2DoubleOpenHashMap();
			itemTotals.defaultReturnValue(0.0);
			Long2IntMap itemCounts = new Long2IntOpenHashMap();
			itemCounts.defaultReturnValue(0);
			
			Cursor<Rating> ratings = data.getRatings();
			try {
				for (Rating r: ratings) {
					long i = r.getItemId();
					double v = r.getRating();
					total += v;
					count++;
					itemTotals.put(i, v + itemTotals.get(i));
					itemCounts.put(i, 1 + itemCounts.get(i));
				}
			} finally {
				ratings.close();
			}
			
			double mean = 0.0;
			if (count > 0) mean = total / count;
			logger.debug("Computed global mean {} for {} items",
					mean, itemTotals.size());
			
			LongIterator items = itemTotals.keySet().iterator();
			while (items.hasNext()) {
				long iid = items.nextLong();
				int ct = itemCounts.get(iid);
				double t = itemTotals.get(iid);
				double avg = 0.0;
				if (ct > 0) avg = t / ct;
				avg = avg - mean;
				itemTotals.put(iid, avg);
			}
			return create(count > 0 ? total / count : 0.0, itemTotals);
		}
		
		protected RatingPredictor create(double globalMean, Long2DoubleMap itemMeans) {
			return new ItemMeanPredictor(globalMean, itemMeans);
		}
		
	}
}
