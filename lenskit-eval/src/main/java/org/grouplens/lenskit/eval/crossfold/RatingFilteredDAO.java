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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.BasicUserRatingProfile;
import org.grouplens.lenskit.data.LongCursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SortOrder;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * A data source which filters another data source with a user predicate.
 * 
 * FIXME: integrate with a build context
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class RatingFilteredDAO implements RatingDataAccessObject {
    private static final Logger logger = LoggerFactory.getLogger(RatingFilteredDAO.class);
    private RatingDataAccessObject base;
    private final Long2ObjectMap<SparseVector> userQueries;

    public RatingFilteredDAO(RatingDataAccessObject base, Long2ObjectMap<SparseVector> q) {
        this.base = base;
        userQueries = q;
    }

    private class RatingFilter implements Predicate<Rating> {
        @Override public boolean apply(Rating r) {
            SparseVector uq = userQueries.get(r.getUserId());
            return uq == null || !uq.containsKey(r.getItemId());
        }
    }

    private class UserRatingFilter implements Predicate<Rating> {
        final long userId;
        final SparseVector query;
        public UserRatingFilter(long user) {
            userId = user;
            query = userQueries.get(user);
        }
        @Override public boolean apply(Rating r) {
            assert userId == r.getUserId();
            return query == null || !query.containsKey(r.getItemId());
        }
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.RatingDataSource#getRatings()
     */
    @Override
    public Cursor<Rating> getRatings() {
        return Cursors.filter(base.getRatings(), new RatingFilter());
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.RatingDataSource#getRatings(org.grouplens.lenskit.data.dao.SortOrder)
     */
    @Override
    public Cursor<Rating> getRatings(SortOrder order) {
        return Cursors.filter(base.getRatings(order), new RatingFilter());
    }

    private AtomicBoolean urpUsed = new AtomicBoolean(false);

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.RatingDataSource#getUserRatingProfiles()
     */
    @Override
    public Cursor<UserRatingProfile> getUserRatingProfiles() {
        if (!urpUsed.get()) {
            logger.warn("Using expensive rating-filtered user rating profile");
            logger.info("Future warnings about slow user rating profiles suppressed.");
            urpUsed.set(true);
        }
        return Cursors.transform(base.getUserRatingProfiles(),
                                 new Function<UserRatingProfile, UserRatingProfile>() {
            public UserRatingProfile apply(final UserRatingProfile p) {
                SparseVector uq = userQueries.get(p.getUser());
                if (uq == null) return p;

                Collection<Rating> ratings = new ArrayList<Rating>();
                for (Rating r: p.getRatings()) {
                    if (!uq.containsKey(r.getItemId()))
                        ratings.add(r);
                }
                return new BasicUserRatingProfile(p.getUser(), ratings);
            }
        });
    }

    @Override
    public Cursor<Rating> getUserRatings(long userId) {
        return Cursors.filter(base.getUserRatings(userId), new UserRatingFilter(userId));
    }

    @Override
    public Cursor<Rating> getUserRatings(long userId, SortOrder order) {
        return Cursors.filter(base.getUserRatings(userId, order), new UserRatingFilter(userId));
    }
    
    @Override
    public Cursor<Rating> getItemRatings(long itemId) {
        return Cursors.filter(base.getItemRatings(itemId), new RatingFilter());
    }

    @Override
    public Cursor<Rating> getItemRatings(long itemId, SortOrder order) {
        return Cursors.filter(base.getItemRatings(itemId, order), new RatingFilter());
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

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.DataSource#getUsers()
     */
    @Override
    public LongCursor getUsers() {
        return base.getUsers();
    }

    @Override
    public int getUserCount() {
        return base.getUserCount();
    }

    @Override
    public void close() {
        base.close();
    }
}
