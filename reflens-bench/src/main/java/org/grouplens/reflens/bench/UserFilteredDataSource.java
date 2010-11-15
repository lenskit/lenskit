/**
 * 
 */
package org.grouplens.reflens.bench;

import it.unimi.dsi.fastutil.longs.LongList;

import java.lang.ref.SoftReference;

import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.Cursors;
import org.grouplens.reflens.data.LongCursor;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.SortOrder;
import org.grouplens.reflens.data.UserRatingProfile;

import com.google.common.base.Predicate;

/**
 * A data source which filters another data source with a user predicate.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserFilteredDataSource implements RatingDataSource {
	private RatingDataSource base;
	private final Predicate<Long> userFilter;
	private final boolean closeBase;
	private SoftReference<LongList> userCache;
	
	public UserFilteredDataSource(RatingDataSource base, Predicate<Long> filter) {
		this(base, false, filter);
	}
	
	public UserFilteredDataSource(RatingDataSource base, boolean closeBase, Predicate<Long> filter) {
		this.base = base;
		this.closeBase = closeBase;
		userFilter = filter;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingDataSource#getRatings()
	 */
	@Override
	public Cursor<Rating> getRatings() {
		return Cursors.filter(base.getRatings(), new RatingPredicate());
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingDataSource#getRatings(org.grouplens.reflens.data.SortOrder)
	 */
	@Override
	public Cursor<Rating> getRatings(SortOrder order) {
		return Cursors.filter(base.getRatings(order), new RatingPredicate());
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingDataSource#getUserRatingProfiles()
	 */
	@Override
	public Cursor<UserRatingProfile> getUserRatingProfiles() {
		return Cursors.filter(base.getUserRatingProfiles(), new RatingProfilePredicate());
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingDataSource#getUserRatings(long)
	 */
	@Override
	public Cursor<Rating> getUserRatings(long userId) {
		if (userFilter.apply(userId))
			return base.getUserRatings(userId);
		else
			return Cursors.empty();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingDataSource#getUserRatings(long, org.grouplens.reflens.data.SortOrder)
	 */
	@Override
	public Cursor<Rating> getUserRatings(long userId, SortOrder order) {
		if (userFilter.apply(userId))
			return base.getUserRatings(userId, order);
		else
			return Cursors.empty();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.DataSource#close()
	 */
	@Override
	public void close() {
		if (closeBase)
			base.close();
	}
	
	@Override
	public int getItemCount() {
		return base.getItemCount();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.DataSource#getItems()
	 */
	@Override
	public LongCursor getItems() {
		return base.getItems();
	}
	
	private LongList getCachedUsers() {
		return userCache == null ? null : userCache.get();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.DataSource#getUsers()
	 */
	@Override
	public LongCursor getUsers() {
		LongList users = getCachedUsers();
		if (users == null)
			return Cursors.makeLongCursor(Cursors.filter(base.getUsers(), userFilter));
		else
			return Cursors.wrap(users);
	}
	
	@Override
	public int getUserCount() {
		LongList users = getCachedUsers();
		if (users == null) {
			users = Cursors.makeList(getUsers());
			userCache = new SoftReference<LongList>(users);
		}
		return users.size();
	}
	
	private class RatingPredicate implements Predicate<Rating> {
		public boolean apply(Rating r) {
			return userFilter.apply(r.getUserId());
		}
	}
	
	private class RatingProfilePredicate implements Predicate<UserRatingProfile> {
		public boolean apply(UserRatingProfile profile) {
			return userFilter.apply(profile.getUser());
		}
	}

}
