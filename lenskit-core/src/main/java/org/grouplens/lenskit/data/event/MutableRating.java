/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.data.pref.Preference;

/**
 * Rating implementation for mutation by fast iterators. It is used in
 * {@link org.grouplens.lenskit.data.dao.DelimitedTextRatingCursor} and similar places.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class MutableRating implements Rating {
    private long eid;
    private long uid;
    private long iid;
    private double value;
    private long timestamp;

    // preference object mirroring this value.
    private final Preference preference = new Preference() {
        @Override
        public long getUserId() {
            return uid;
        }

        @Override
        public long getItemId() {
            return iid;
        }

        @Override
        public double getValue() {
            return value;
        }
    };

    @Override
    public long getId() {
        return eid;
    }

    public void setId(long eid) {
        this.eid = eid;
    }

    @Override
    public long getUserId() {
        return uid;
    }

    public void setUserId(long uid) {
        this.uid = uid;
    }

    @Override
    public long getItemId() {
        return iid;
    }

    public void setItemId(long iid) {
        this.iid = iid;
    }

    @Override
    public Preference getPreference() {
        if (Double.isNaN(value)) {
            return null;
        } else {
            return preference;
        }
    }

    /**
     * Set the rating value. A value of {@link Double#NaN} indicates an unrate
     * event.
     *
     * @param v The rating value.
     */
    public void setRating(double v) {
        value = v;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long ts) {
        timestamp = ts;
    }

    @Override
    public Rating copy() {
        if (Double.isNaN(value)) {
            return new SimpleNullRating(eid, uid, iid, timestamp);
        } else {
            return new SimpleRating(eid, uid, iid, value, timestamp);
        }
    }
}
