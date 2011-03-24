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
/**
 *
 */
package org.grouplens.lenskit.eval.crossfold;

import it.unimi.dsi.fastutil.longs.LongList;

import java.lang.ref.SoftReference;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.Cursors2;
import org.grouplens.lenskit.data.LongCursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SortOrder;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.dao.RatingUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

/**
 * A data source which filters another data source with a user predicate.
 * 
 * FIXME: integrate with a build context
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserFilteredDAO implements RatingDataAccessObject {
    private static final Logger logger = LoggerFactory.getLogger(UserFilteredDAO.class);
    private RatingDataAccessObject base;
    private final Predicate<Long> userFilter;
    private SoftReference<LongList> userCache;

    public UserFilteredDAO(RatingDataAccessObject base, Predicate<Long> filter) {
        this.base = base;
        userFilter = filter;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.RatingDataSource#getRatings()
     */
    @Override
    public Cursor<Rating> getRatings() {
        return org.grouplens.common.cursors.Cursors.filter(base.getRatings(), new RatingPredicate());
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.RatingDataSource#getRatings(org.grouplens.lenskit.data.dao.SortOrder)
     */
    @Override
    public Cursor<Rating> getRatings(SortOrder order) {
        return org.grouplens.common.cursors.Cursors.filter(base.getRatings(order), new RatingPredicate());
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.RatingDataSource#getUserRatingProfiles()
     */
    @Override
    public Cursor<UserRatingProfile> getUserRatingProfiles() {
        return org.grouplens.common.cursors.Cursors.filter(base.getUserRatingProfiles(), new RatingProfilePredicate());
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.RatingDataSource#getUserRatings(long)
     */
    @Override
    public Cursor<Rating> getUserRatings(long userId) {
        if (userFilter.apply(userId))
            return base.getUserRatings(userId);
        else
            return org.grouplens.common.cursors.Cursors.empty();
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.RatingDataSource#getUserRatings(long, org.grouplens.lenskit.data.dao.SortOrder)
     */
    @Override
    public Cursor<Rating> getUserRatings(long userId, SortOrder order) {
        if (userFilter.apply(userId))
            return base.getUserRatings(userId, order);
        else
            return org.grouplens.common.cursors.Cursors.empty();
    }

    @Override
    public int getItemCount() {
        return base.getItemCount();
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.DataSource#getItems()
     */
    @Override
    public LongCursor getItems() {
        return base.getItems();
    }

    private LongList getCachedUsers() {
        return userCache == null ? null : userCache.get();
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.DataSource#getUsers()
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

    @Override
    public void addRatingUpdateListener(RatingUpdateListener listener) {
        /* we do not support update listeners. */
    }

    @Override
    public void removeRatingUpdateListener(RatingUpdateListener listener) {
        /* we do not support update listeners */
    }

}
