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
package org.grouplens.reflens.eval.crossfold;

import it.unimi.dsi.fastutil.longs.LongList;

import java.lang.ref.SoftReference;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.reflens.data.Cursors2;
import org.grouplens.reflens.data.LongCursor;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.SortOrder;
import org.grouplens.reflens.data.UserRatingProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

/**
 * A data source which filters another data source with a user predicate.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserFilteredDataSource implements RatingDataSource {
	private static final Logger logger = LoggerFactory.getLogger(UserFilteredDataSource.class);
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
		return org.grouplens.common.cursors.Cursors.filter(base.getRatings(), new RatingPredicate());
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingDataSource#getRatings(org.grouplens.reflens.data.SortOrder)
	 */
	@Override
	public Cursor<Rating> getRatings(SortOrder order) {
		return org.grouplens.common.cursors.Cursors.filter(base.getRatings(order), new RatingPredicate());
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingDataSource#getUserRatingProfiles()
	 */
	@Override
	public Cursor<UserRatingProfile> getUserRatingProfiles() {
		return org.grouplens.common.cursors.Cursors.filter(base.getUserRatingProfiles(), new RatingProfilePredicate());
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingDataSource#getUserRatings(long)
	 */
	@Override
	public Cursor<Rating> getUserRatings(long userId) {
		if (userFilter.apply(userId))
			return base.getUserRatings(userId);
		else
			return org.grouplens.common.cursors.Cursors.empty();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingDataSource#getUserRatings(long, org.grouplens.reflens.data.SortOrder)
	 */
	@Override
	public Cursor<Rating> getUserRatings(long userId, SortOrder order) {
		if (userFilter.apply(userId))
			return base.getUserRatings(userId, order);
		else
			return org.grouplens.common.cursors.Cursors.empty();
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
		if (users == null) {
			logger.trace("Returning fresh user list");
			return Cursors2.makeLongCursor(org.grouplens.common.cursors.Cursors.filter(base.getUsers(), userFilter));
		} else {
			logger.trace("Returning cached user list");
			return Cursors2.wrap(users);
		}
	}

	@Override
	public int getUserCount() {
		LongList users = getCachedUsers();
		if (users == null) {
			logger.trace("Caching user list");
			users = Cursors2.makeList(getUsers());
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
