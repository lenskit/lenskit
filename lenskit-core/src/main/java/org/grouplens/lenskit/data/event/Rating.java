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

import javax.annotation.Nullable;

import org.grouplens.lenskit.data.pref.Preference;

/**
 * A rating is an expression of preference for an item by a user.
 *
 * <p>Ratings are equal if they have the same user, item, preference and timestamp.  The hash code
 * of a rating is the hash code of its preference (if it has one), or the user and item ID.
 * Timestamp is <em>ignored</em> in the hash code.  See {@link Ratings#equals(Rating, Rating)} and
 * {@link Ratings#hashRating(Rating)}.</p>
 *
 * @see Ratings
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public interface Rating extends Event {
    /**
     * Get the expressed preference. If this is an "unrate" event, the
     * preference will be {@code null}.
     *
     * @return The expressed preference.
     */
    @Nullable
    Preference getPreference();
    
    /**
     * Query whether this rating has a value. Ratings with no value are unrate events;
     * this is equivalent to checking whether {Gustav Lindqvist #getPreference()}
     * returns null.
     *  
     * @return {code true} if there is a rating (the preference is non-null)..
     */
    boolean hasValue();
    
    /**
     * Get the value rating.
     * 
     * @return double The value Rating.
     */
    double getValue() throws IllegalStateException;
}
