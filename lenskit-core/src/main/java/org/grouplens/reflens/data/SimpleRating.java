package org.grouplens.reflens.data;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A simple rating immutable rating implementation, storing ratings in fields.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ThreadSafe
public final class SimpleRating implements Rating {
	private final long userId;
	private final long itemId;
	private final double rating;
	private final long timestamp;
	
	/**
	 * Construct a rating without a timestamp.
	 * @param uid The user ID.
	 * @param iid The item ID.
	 * @param r The rating value.
	 */
	public SimpleRating(long uid, long iid, double r) {
		this(uid, iid, r, -1);
	}
	
	/**
	 * Construct a rating with a timestamp.
	 * @param uid The user ID.
	 * @param iid The item ID.
	 * @param r The rating value.
	 * @param ts The rating timestamp.
	 */
	public SimpleRating(long uid, long iid, double r, long ts) {
		userId = uid;
		itemId = iid;
		rating = r;
		timestamp = ts;
	}
	
	public long getUserId() {
		return userId;
	}
	
	public long getItemId() {
		return itemId;
	}
	
	public double getRating() {
		return rating;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}
