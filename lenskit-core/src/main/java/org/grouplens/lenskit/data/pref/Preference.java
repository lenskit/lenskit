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
package org.grouplens.lenskit.data.pref;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A real-valued preference a user has for an item. Preferences can be
 * articulated by the user in the form of ratings, or they may be predicted or
 * otherwise computed. The distinction will generally be apparent from context.
 *
 * <p>
 * Instances of this class should generally be immutable (or at least treated as
 * such). All exceptions must be clearly documented, and should only be in
 * contexts such as fast iteration.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public abstract class Preference {
    /**
     * Get the ID of the user whose preference this is.
     *
     * @return The user ID.
     */
    public abstract long getUserId();

    /**
     * Get the ID of the item the preference is for.
     *
     * @return The item ID.
     */
    public abstract long getItemId();

    /**
     * Get the preference value.
     *
     * @return The preference value.
     */
    public abstract double getValue();

    /**
     * Compare two preferences for equality. Preferences are equal if their users,
     * items, and values are equal.
     *
     * @param obj The object to compare.
     * @return {@code true} if the object compares equal to this.
     */
    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj instanceof Preference) {
            Preference op = (Preference) obj;
            return new EqualsBuilder()
                    .append(getUserId(), op.getUserId())
                    .append(getItemId(), op.getItemId())
                    .append(getValue(), op.getValue())
                    .isEquals();
        } else {
            return false;
        }
    }

    /**
     * Hash a preference.
     */
    @Override
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(getItemId())
                .append(getUserId())
                .append(getValue())
                .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("Preference(u=%d, i=%d, v=%.2f", getUserId(), getItemId(), getValue());
    }
}
