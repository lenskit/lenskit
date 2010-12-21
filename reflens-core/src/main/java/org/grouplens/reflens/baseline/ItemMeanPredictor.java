/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

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

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rating predictor that returns the item's mean rating for all predictions.
 * 
 * If the item has no ratings, the global mean rating is returned.
 * 
 * This implements the baseline predictor <i>p<sub>u,i</sub> = µ + b<sub>i</sub></i>,
 * where <i>b<sub>i</sub></i> is the item's average rating (less the global
 * mean µ).
 * 
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
	public RatingVector predict(long user, RatingVector ratings,
			Collection<Long> items) {
		RatingVector predictions = new RatingVector();
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
	public ScoredId predict(long user, RatingVector ratings, long item) {
		return new ScoredId(item, getItemMean(item));
	}
	
	protected double getItemMean(long id) {
		return globalMean + itemAverages.get(id);
	}

	/**
	 * Predictor builder for the item mean predictor.
	 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
	 *
	 */
	public static class Builder implements RatingPredictorBuilder {
		private static Logger logger = LoggerFactory.getLogger(Builder.class);

		@Override
		public RatingPredictor build(RatingDataSource data) {
			// We iterate the loop to compute the global and per-item mean
			// ratings.  Subtracting the global mean from each per-item mean
			// is equivalent to averaging the offsets from the global mean, so
			// we can compute the means in parallel and subtract after a single
			// pass through the data.
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
			
			LongIterator items = itemCounts.keySet().iterator();
			while (items.hasNext()) {
				long iid = items.nextLong();
				int ct = itemCounts.get(iid);
				double t = itemTotals.get(iid);
				double avg = 0.0;
				if (ct > 0) avg = t / ct - mean;
				itemTotals.put(iid, avg);
			}
			return create(mean, itemTotals);
		}
		
		protected RatingPredictor create(double globalMean, Long2DoubleMap itemMeans) {
			return new ItemMeanPredictor(globalMean, itemMeans);
		}
		
	}
}
