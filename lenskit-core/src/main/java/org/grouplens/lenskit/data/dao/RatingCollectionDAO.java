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
package org.grouplens.lenskit.data.dao;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.BasicUserRatingProfile;
import org.grouplens.lenskit.data.Cursors2;
import org.grouplens.lenskit.data.LongCursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.SortOrder;
import org.grouplens.lenskit.data.UserRatingProfile;

/**
 * Data source backed by a collection of ratings.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class RatingCollectionDAO extends AbstractRatingDataAccessObject {
    public static class Manager implements DataAccessObjectManager<RatingCollectionDAO> {
        private final Collection<Rating> ratings;
        private transient RatingCollectionDAO singleton;
        
        public Manager(Collection<Rating> ratings) {
            this.ratings = ratings;
        }
        
        @Override
        public RatingCollectionDAO open() {
            if (singleton == null) {
                singleton = new RatingCollectionDAO(ratings);
                singleton.requireItemCache();
                singleton.requireUserCache();
            }
            
            return singleton;
        }
    }
    
    private Collection<Rating> ratings;
    private Long2ObjectMap<UserRatingProfile> users;
    private Long2ObjectMap<ArrayList<Rating>> items;

    /**
     * Construct a new data source from a collection of ratings.
     * @param ratings The ratings to use.
     */
    public RatingCollectionDAO(Collection<Rating> ratings) {
        this.ratings = ratings;
    }

    private synchronized void requireUserCache() {
        if (users == null) {
            logger.debug("Caching user rating profiles");
            Long2ObjectMap<ArrayList<Rating>> ratingCs =
                new Long2ObjectOpenHashMap<ArrayList<Rating>>();
            for (Rating r: ratings) {
                final long uid = r.getUserId();
                ArrayList<Rating> userRatings = ratingCs.get(uid);
                if (userRatings == null) {
                    userRatings = new ArrayList<Rating>(20);
                    ratingCs.put(uid, userRatings);
                }
                userRatings.add(r);
            }
            users = new Long2ObjectOpenHashMap<UserRatingProfile>(ratingCs.size());
            for (Long2ObjectMap.Entry<ArrayList<Rating>> e: ratingCs.long2ObjectEntrySet()) {
                e.getValue().trimToSize();
                Collections.sort(e.getValue(), Ratings.ITEM_TIME_COMPARATOR);
                users.put(e.getLongKey(), new BasicUserRatingProfile(e));
            }
        }
    }
    
    private synchronized void requireItemCache() {
        if (items == null) {
            logger.debug("Caching item rating collections");
            items = new Long2ObjectOpenHashMap<ArrayList<Rating>>();
            for (Rating r: ratings) {
                final long iid = r.getItemId();
                ArrayList<Rating> itemRatings = items.get(iid);
                if (itemRatings == null) {
                    itemRatings = new ArrayList<Rating>(20);
                    items.put(iid, itemRatings);
                }
                itemRatings.add(r);
            }
            for (ArrayList<Rating> rs: items.values()) {
                rs.trimToSize();
            }
        }
    }

    @Override
    public LongCursor getUsers() {
        requireUserCache();
        return Cursors2.wrap(users.keySet());
    }

    @Override
    public Cursor<Rating> getUserRatings(long user, SortOrder order) {
        requireUserCache();
        Collection<Rating> ratings = users.get(user).getRatings();
        if (ratings == null) return Cursors.empty();


        Comparator<Rating> comp = null;
        switch (order) {
        case ANY:
            break;
        case USER:
            comp = Ratings.USER_COMPARATOR;
            break;
        case ITEM:
            comp = Ratings.ITEM_COMPARATOR;
            break;
        case TIMESTAMP:
            comp = Ratings.TIMESTAMP_COMPARATOR;
            break;
        default:
            throw new UnsupportedQueryException();
        }
        if (comp != null) {
            List<Rating> sratings = new ArrayList<Rating>(ratings);
            Collections.sort(sratings, comp);
            ratings = sratings;
        }
        return Cursors.wrap(ratings);
    }

    @Override
    public Cursor<UserRatingProfile> getUserRatingProfiles() {
        requireUserCache();
        return Cursors.wrap(users.values().iterator());
    }
    
    @Override
    public Cursor<Rating> getItemRatings(long item, SortOrder order) {
        requireItemCache();
        
        List<Rating> ratings = items.get(item);
        if (ratings == null) return Cursors.empty();
        
        Comparator<Rating> comp = null;
        switch (order) {
        case ANY:
            break;
        case USER:
            comp = Ratings.USER_COMPARATOR;
            break;
        case ITEM:
            comp = Ratings.ITEM_COMPARATOR;
            break;
        case TIMESTAMP:
            comp = Ratings.TIMESTAMP_COMPARATOR;
            break;
        default:
            throw new UnsupportedQueryException();
        }
        if (comp != null) {
            ratings = new ArrayList<Rating>(ratings);
            Collections.sort(ratings, comp);
        }
        return Cursors.wrap(ratings);
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.AbRatingDataAccessObjectAccessObject#getRatings()
     */
    @Override
    public Cursor<Rating> getRatings() {
        return Cursors.wrap(ratings);
    }

    @Override
    public void close() {
        // do nothing, there is nothing to close
    }
}
