/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.data;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.grouplens.common.cursors.AbstractCursor;
import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.common.cursors.FilteredCursor;

import com.google.common.base.Predicate;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractRatingDataSource implements RatingDataSource {
    private SoftReference<LongSet> itemCache;
    private SoftReference<LongSet> userCache;

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.RatingDataSource#getRatings(org.grouplens.lenskit.data.SortOrder)
     */
    @Override
    public abstract Cursor<Rating> getRatings();

    /**
     * Implement {@link RatingDataSource#getRatings(SortOrder)} by sorting the
     * output of {@link #getRatings()}.
     */
    @Override
    public Cursor<Rating> getRatings(SortOrder order) {
        Comparator<Rating> comp = null;

        switch (order) {
        case ANY:
            return getRatings();
        case TIMESTAMP:
            comp = new Comparator<Rating>() {
                public int compare(Rating r1, Rating r2) {
                    long ts1 = r1.getTimestamp();
                    long ts2 = r2.getTimestamp();
                    if (ts1 > ts2)
                        return 1;
                    else if (ts1 < ts2)
                        return -1;
                    else
                        return 0;
                }
            };
            break;
        case USER:
            comp = new Comparator<Rating>() {
                public int compare(Rating r1, Rating r2) {
                    long u1 = r1.getUserId();
                    long u2 = r2.getUserId();
                    if (u1 > u2)
                        return 1;
                    else if (u1 < u2)
                        return -1;
                    else
                        return 0;
                }
            };
            break;
        case ITEM:
            comp = new Comparator<Rating>() {
                public int compare(Rating r1, Rating r2) {
                    long i1 = r1.getItemId();
                    long i2 = r2.getItemId();
                    if (i1 > i2)
                        return 1;
                    else if (i1 < i2)
                        return -1;
                    else
                        return 0;
                }
            };
            break;
        default:
            assert false;
        }

        ArrayList<Rating> ratings = Cursors.makeList(getRatings());
        Collections.sort(ratings, comp);
        return Cursors.wrap(ratings.iterator());
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.RatingDataSource#getUserRatingProfiles()
     */
    @Override
    public Cursor<UserRatingProfile> getUserRatingProfiles() {
        return new UserProfileCursor(getRatings(SortOrder.USER));
    }

    /**
     * Implement {@link RatingDataSource#getUserRatings(long)} by delegating to
     * {@link #getUserRatings(long, SortOrder)}.
     */
    @Override
    public Cursor<Rating> getUserRatings(long userId) {
        return getUserRatings(userId, SortOrder.ANY);
    }

    /**
     * Implement {@link RatingDataSource#getUserRatings(long, SortOrder)} by
     * filtering the output of {@link #getRatings(SortOrder)}.
     */
    @Override
    public Cursor<Rating> getUserRatings(final long userId, SortOrder order) {
        Cursor<Rating> base = getRatings(order);
        return new FilteredCursor<Rating>(base, new Predicate<Rating>() {
            public boolean apply(Rating r) {
                return r.getUserId() == userId;
            }
        });
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.DataSource#close()
     */
    @Override
    public void close() {
        /* no-op */
    }

    private LongSet getItemSet() {
        LongSet items = null;
        if (itemCache != null)
            items = itemCache.get();

        if (items == null) {
            items = new LongOpenHashSet();
            Cursor<Rating> ratings = getRatings();
            try {
                for (Rating r: ratings) {
                    items.add(r.getItemId());
                }
            } finally {
                ratings.close();
            }
            itemCache = new SoftReference<LongSet>(items);
        }
        return items;
    }

    /**
     * Implement {@link RatingDataSource#getItems()} by processing the output
     * of {@link #getRatings()}.
     */
    @Override
    public LongCursor getItems() {
        return Cursors2.wrap(getItemSet());
    }

    @Override
    public int getItemCount() {
        return getItemSet().size();
    }

    private LongSet getUserSet() {
        LongSet users = null;
        if (userCache != null)
            users = userCache.get();

        if (users == null) {
            users = new LongOpenHashSet();
            Cursor<Rating> ratings = getRatings();
            try {
                for (Rating r: ratings) {
                    users.add(r.getUserId());
                }
            } finally {
                ratings.close();
            }
            userCache = new SoftReference<LongSet>(users);
        }
        return users;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.DataSource#getUsers()
     */
    @Override
    public LongCursor getUsers() {
        return Cursors2.wrap(getUserSet());
    }

    @Override
    public int getUserCount() {
        return getUserSet().size();
    }

    static class UserProfileCursor extends AbstractCursor<UserRatingProfile> {
        private Cursor<Rating> cursor;
        private Rating lastRating;

        public UserProfileCursor(Cursor<Rating> cursor) {
            this.cursor = cursor;
            lastRating = null;
        }

        @Override
        public void close() {
            if (cursor != null)
                cursor.close();
            cursor = null;
            lastRating = null;
        }

        @Override
        public boolean hasNext() {
            return cursor != null && (lastRating != null || cursor.hasNext());
        }

        @Override
        public UserRatingProfile next() {
            if (cursor == null) throw new NoSuchElementException();
            long uid;
            List<Rating> ratings = new ArrayList<Rating>();
            if (lastRating == null)
                lastRating = cursor.next();
            uid = lastRating.getUserId();
            do {
                ratings.add(lastRating);
                if (cursor.hasNext())
                    lastRating = cursor.next();
                else
                    lastRating = null;
            } while (lastRating != null && lastRating.getUserId() == uid);

            return new BasicUserRatingProfile(uid, ratings);
        }
    }
}
