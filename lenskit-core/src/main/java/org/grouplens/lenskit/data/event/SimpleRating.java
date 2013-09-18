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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
final class SimpleRating implements Rating, Preference {
    final long timestamp;
    final long user;
    final long item;
    final double value;

    /**
     * Construct a rating with a timestamp and value.
     *
     * @param uid The user ID.
     * @param iid The item ID.
     * @param v   The rating value.
     * @param ts  The event timestamp.
     */
    SimpleRating(long uid, long iid, double v, long ts) {
        timestamp = ts;
        user = uid;
        item = iid;
        value = v;
    }

    @Override 
    public final boolean hasValue() {
        if (Double.isNaN(value)) {
            return false;
        } else {
            return true;
        }
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
    public final double getValue() throws IllegalStateException {
        if (this.hasValue()) {
            return value;
        } else {
            String msg = "There is no rating";
            throw new IllegalStateException(msg);
        }
    }
    
    @Override
    public final long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Rating) {
            Rating ro = (Rating) o;
            Preference op = ro.getPreference();
            if (op != null) {
                return new EqualsBuilder().append(user, ro.getUserId())
                                          .append(item, ro.getItemId())
                                          .append(value, op.getValue())
                                          .append(timestamp,  ro.getTimestamp())
                                          .isEquals();
            } else {
                return false;
            }
        } else if (o instanceof Preference) {
            Preference op = (Preference) o;
            return new EqualsBuilder().append(user, op.getUserId())
                                      .append(item, op.getItemId())
                                      .append(value, op.getValue())
                                      .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(user)
                                    .append(item)
                                    .append(value)
                                    .toHashCode();
    }
}
