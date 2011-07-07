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
package org.grouplens.lenskit.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Basic user rating profile backed by a collection of ratings.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class BasicUserRatingProfile implements UserRatingProfile {

    private long user;
    private Collection<Rating> ratings;
    private transient volatile SparseVector vector;

    /**
     * Construct a new basic user profile.
     * @param user The user ID.
     * @param ratings The user's rating collection. The collection is <b>not</b>
     * copied, so make sure that it is not changed after the user rating profile
     * takes ownership.
     */
    public BasicUserRatingProfile(long user, Collection<Rating> ratings) {
        this.user = user;
        this.ratings = Collections.unmodifiableCollection(ratings);
    }

    /**
     * Construct a profile from a map entry.
     * @param entry
     */
    public BasicUserRatingProfile(Map.Entry<Long, ? extends Collection<Rating>> entry) {
        this(entry.getKey(), entry.getValue());
    }

    @Override
    public double getRating(long item) {
        return getRatingVector().get(item);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SparseVector getRatingVector() {
        if (vector == null) {
            vector = Ratings.userRatingVector(getRatings());
        }
        return vector;
    }

    @Override
    public Collection<Rating> getRatings() {
        return ratings;
    }

    @Override
    public long getUser() {
        return user;
    }

}
