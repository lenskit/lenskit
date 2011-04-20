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
    private Collection<Rating> ratings;
    private Long2ObjectMap<UserRatingProfile> users;

    /**
     * Construct a new data source from a collection of ratings.
     * @param ratings The ratings to use.
     */
    public RatingCollectionDAO(Collection<Rating> ratings) {
        this.ratings = ratings;
    }
    
    private synchronized void requireUserCache() {
    	if (users == null) {
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
    		    users.put(e.getLongKey(), new BasicUserRatingProfile(e));
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
    	
    	ArrayList<Rating> copy;
    	
    	switch (order) {
    	case ANY:
    		return Cursors.wrap(ratings);
    	case TIMESTAMP:
    		copy = new ArrayList<Rating>(ratings);
    		Collections.sort(copy, Ratings.TIMESTAMP_COMPARATOR);
    		return Cursors.wrap(copy);
    	default:
    		throw new UnsupportedQueryException();
    	}
    }
    
    @Override
    public Cursor<UserRatingProfile> getUserRatingProfiles() {
    	requireUserCache();
    	return Cursors.wrap(users.values().iterator());
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.data.dao.AbRatingDataAccessObjectAccessObject#getRatings()
     */
    @Override
    public Cursor<Rating> getRatings() {
        return Cursors.wrap(ratings);
    }

}
