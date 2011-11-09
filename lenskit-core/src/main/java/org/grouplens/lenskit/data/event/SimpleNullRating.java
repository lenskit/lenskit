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

import javax.annotation.concurrent.Immutable;

import org.grouplens.lenskit.data.pref.Preference;

/**
 * Simple implementation of a null rating (unrate event).
 *
 * @see SimpleRating
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Immutable
public class SimpleNullRating implements Rating {
    private final long id;
    private final long userId;
    private final long itemId;
    private final long timestamp;

    /**
     * Construct a new null rating.
     *
     * @param id The event ID.
     * @param uid The user ID.
     * @param iid The item ID.
     * @param ts The event timestamp.
     */
    public SimpleNullRating(long id, long uid, long iid, long ts) {
        this.id = id;
        userId = uid;
        itemId = iid;
        timestamp = ts;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public long getItemId() {
        return itemId;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Preference getPreference() {
        return null;
    }

    @Override @Deprecated
    public double getRating() {
        return Double.NaN;
    }

    @Override
    public Rating clone() {
        /* this object is immutable. Just return it. */
        return this;
    }
}
