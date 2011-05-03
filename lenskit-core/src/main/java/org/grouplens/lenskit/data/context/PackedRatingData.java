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
package org.grouplens.lenskit.data.context;

import org.grouplens.lenskit.data.AbstractRating;
import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.data.SimpleIndexedRating;

/**
 * Data storage for packed build contexts.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
final class PackedRatingData {
	final int[] users;
	final int[] items;
	final double[] values;
	final long[] timestamps;
	final Index itemIndex;
	final Index userIndex;
	
	public PackedRatingData(int[] users, int[] items, double[] values, long[] timestamps, Index itemIndex, Index userIndex) {
		this.users = users;
		this.items = items;
		this.values = values;
		this.timestamps = timestamps;
		this.itemIndex = itemIndex;
		this.userIndex = userIndex;
	}
	
	public IndirectRating makeIndirectRating(int index) {
		return new IndirectRating(index);
	}
	
	public IndexedRating makeRating(int index) {
		final int uidx = users[index];
		final int iidx = items[index];
		final long user = userIndex.getId(uidx);
		final long item = itemIndex.getId(iidx);
		final double v = values[index];
		final long ts = timestamps == null ? -1 : timestamps[index];
		return new SimpleIndexedRating(user, item, v, ts, uidx, iidx);
	}
	
	final class IndirectRating extends AbstractRating implements IndexedRating {
		int index;
		public IndirectRating(int index) {
			this.index = index;
		}
		@Override
		public int getItemIndex() {
			return items[index];
		}
		@Override
		public int getUserIndex() {
			return users[index];
		}
		@Override
		public long getItemId() {
			return itemIndex.getId(getItemIndex());
		}
		@Override
		public double getRating() {
			return values[index];
		}
		@Override
		public long getTimestamp() {
			return timestamps == null ? -1 : timestamps[index];
		}
		@Override
		public long getUserId() {
			return userIndex.getId(getUserIndex());
		}
		
		public IndexedRating clone() {
		    return (IndexedRating) super.clone();
		}
	}
}
