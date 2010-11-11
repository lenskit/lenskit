/**
 * 
 */
package org.grouplens.reflens.data;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public final class Rating {
	private final long userId;
	private final long itemId;
	private final double rating;
	private final long timestamp;
	
	public Rating(long uid, long iid, double r) {
		this(uid, iid, r, -1);
	}
	
	public Rating(long uid, long iid, double r, long ts) {
		userId = uid;
		itemId = iid;
		rating = r;
		timestamp = ts;
	}
	
	public final long getUserId() {
		return userId;
	}
	
	public final long getItemId() {
		return itemId;
	}
	
	public final double getRating() {
		return rating;
	}
	
	public final long getTimestamp() {
		return timestamp;
	}
}
