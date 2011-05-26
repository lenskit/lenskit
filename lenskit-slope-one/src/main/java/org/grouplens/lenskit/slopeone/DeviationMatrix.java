/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.slopeone;

import org.grouplens.lenskit.data.snapshot.RatingSnapshot;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * A matrix used to store the deviation between a pair of items.
 *
 */
public class DeviationMatrix {

	private Long2ObjectOpenHashMap<Long2DoubleOpenHashMap> deviations;

	public DeviationMatrix(RatingSnapshot snap) {
		deviations = new Long2ObjectOpenHashMap<Long2DoubleOpenHashMap>();
		for (long itemId : snap.getItemIds()) deviations.put(itemId, new Long2DoubleOpenHashMap());
		long[] items = snap.getItemIds().toLongArray();
		for (int i = 0; i < items.length-1; i++) {
			for (int j = i; j < items.length; j++)
				put(items[i], items[j], Double.NaN);
		}
	}

	/**
	 * Stores a value in the matrix.
	 * @param item1 The first item of the pair
	 * @param item2 The second item of the pair
	 * @param n The average difference in ratings (i.e. The ratings of 
	 * <var>item2</var> subtracted from those of <var>item1</var>) between 
	 * the two items
	 */
	public void put (long item1, long item2, double n) {
		if (item1 != item2) {
			if (item1 < item2) deviations.get(item1).put(item2, n);
			else deviations.get(item2).put(item1, -n);
		}
	}

	/**
	 * Retrieves a value from the matrix.
	 * @param item1 The first item of the pair
	 * @param item2 The second item of the pair
	 * @return The average difference in ratings of <var>item1</var> with
	 * respect to <var>item2</var>.
	 */
	public double get(long item1, long item2) {
		if (item1 == item2) return 0;
		else if (item1 < item2) {
			Long2DoubleOpenHashMap map = deviations.get(item1);
			if (map == null) return Double.NaN;
			else return map.get(item2);
		}
		else {
			Long2DoubleOpenHashMap map = deviations.get(item2);
			if (map == null) return Double.NaN;
			else return -map.get(item1);
		}
	}

	/**
	 * Fills the matrix with deviation values based on item pair
	 * rating differentials already stored in the matrix and a matrix containing
	 * the number of users that have co-rated each item pair.
	 * @param comp A <tt>DeviationComputer</tt> object that is used to
	 * calculate each deviation.
	 * @param commonUsers A <tt>CoratingMatrix</tt> object that stores the number of users
	 * that rated both items in each pair.
	 */
	void compute(DeviationComputer comp, CoratingMatrix commonUsers) {
		for (long item1 : deviations.keySet()) {
			for (long item2 : deviations.get(item1).keySet()) {
				if (commonUsers.get(item1, item2) != 0)
					put(item1, item2, comp.findDeviation(get(item1, item2), commonUsers.get(item1, item2)));
			}
		}
	}
}