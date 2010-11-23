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
public class UserItemMeanPredictor extends ItemMeanPredictor {

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
	
	public static class Builder extends ItemMeanPredictor.Builder {
		@Override
		public RatingPredictor create(double globalMean, Long2DoubleMap itemMeans) {
			return new UserItemMeanPredictor(globalMean, itemMeans);
		}
	}

}
