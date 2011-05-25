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

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 *  A symmetric matrix used to store the number of users that have co-rated
 *  a pair of items.
 * */
public class CoratingMatrix {

	private Long2ObjectOpenHashMap<Long2IntOpenHashMap> commonUsers;

	public CoratingMatrix(RatingSnapshot snap) {
		commonUsers = new Long2ObjectOpenHashMap<Long2IntOpenHashMap>();
		for (long itemId : snap.getItemIds())
			commonUsers.put(itemId, new Long2IntOpenHashMap());

	}

	/**
	 * Stores a value in the matrix. Since the matrix is symmetric, only one
	 * value needs to be stored for each pair of items.
	 * @param item1 The first item of the pair
	 * @param item2 The second item of the pair
	 * @param n The number of users who have rated both <var>item1</var> and <var>item2</var>
	 */
	public void put(long item1, long item2, int n) {
		if (item1 != item2) {
			if (item1 < item2) commonUsers.get(item1).put(item2, n);
			else commonUsers.get(item2).put(item1, n);
		}
	}

	/**
	 * Retrieves a value from the matrix.
	 * @param item1 The first item of the pair
	 * @param item2 The second item of the pair
	 * @return The number of users that have rated both <var>item1</var> and <var>item2</var>
	 */
	public int get(long item1, long item2) {
		if (item1 == item2) return 0;
		else if (item1 < item2) {
			Long2IntOpenHashMap map = commonUsers.get(item1);
			if (map == null) return 0;
			else return map.get(item2);
		}
		else {
			Long2IntOpenHashMap map = commonUsers.get(item2);
			if (map == null) return 0;
			else return map.get(item1);
		}
	}
}