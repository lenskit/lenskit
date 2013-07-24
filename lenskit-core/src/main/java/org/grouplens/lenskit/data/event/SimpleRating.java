/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A simple rating immutable rating implementation, storing ratings in fields.
 * This class is not intended to be derived, so its key methods are
 * {@code final}.
 *
 * <p>This implementation only supports set ratings; for null ratings (unrate
 * events), use {@link SimpleNullRating}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
@Immutable
final class SimpleRating extends AbstractEvent implements Rating, Preference {
    final long eventId;
    final long timestamp;
    final long user;
    final long item;
    final double value;

    /**
     * Construct a rating with a timestamp and value.
     *
     * @param eid The event ID.
     * @param uid The user ID.
     * @param iid The item ID.
     * @param v   The rating value.
     * @param ts  The event timestamp.
     */
    SimpleRating(long eid, long uid, long iid, double v, long ts) {
        eventId = eid;
        timestamp = ts;
        user = uid;
        item = iid;
        value = v;
    }

    @Override
    public long getId() {
        return eventId;
    }

    @Override
    public final long getUserId() {
        return user;
    }

    @Override
    public final long getItemId() {
        return item;
    }

    @Override
    @Nonnull
    public final Preference getPreference() {
        return this;
    }

    @Override
    public final long getTimestamp() {
        return timestamp;
    }

    @Override
    public final double getValue() {
        return value;
    }
}
