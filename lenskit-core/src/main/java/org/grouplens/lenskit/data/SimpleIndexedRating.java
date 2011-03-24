/**
 * 
 */
package org.grouplens.lenskit.data;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SimpleIndexedRating extends SimpleRating implements IndexedRating {
	final int userIndex, itemIndex;

	public SimpleIndexedRating(long uid, long iid, double r, long ts, int uidx, int iidx) {
		super(uid, iid, r, ts);
		userIndex = uidx;
		itemIndex = iidx;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.IndexedRating#getItemIndex()
	 */
	@Override
	final public int getItemIndex() {
		return userIndex;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.IndexedRating#getUserIndex()
	 */
	@Override
	final public int getUserIndex() {
		return itemIndex;
	}
}
