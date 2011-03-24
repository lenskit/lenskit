package org.grouplens.lenskit.data.context;

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
	
	final class IndirectRating implements IndexedRating {
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
	}
}
