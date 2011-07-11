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
package org.grouplens.lenskit.data.event;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A simple rating immutable rating implementation, storing ratings in fields.
 * This class is not intended to be derived, so its key methods are
 * <code>final</code>.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ThreadSafe
public class SimpleRating extends AbstractRating {
    final long userId;
    final long itemId;
    final double rating;
    final long timestamp;

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

    @Override
    final public long getUserId() {
        return userId;
    }

    @Override
    final public long getItemId() {
        return itemId;
    }

    @Override
    final public double getRating() {
        return rating;
    }

    @Override
    final public long getTimestamp() {
        return timestamp;
    }
}
