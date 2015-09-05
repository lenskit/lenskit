/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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

import org.lenskit.data.ratings.PackedRatingMatrix;
import org.lenskit.data.ratings.Preference;

/**
 * A Preference that also provides 0-based indices for the user, item, and itself.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public interface IndexedPreference extends Preference {
    /**
     * Get the preference index. Each indexed preference (within a given context,
     * such as a {@link PackedRatingMatrix})
     * has a unique, contiguous, zero-based index. This is to make it easy to
     * store additional information related to preferences for efficient learning
     * scenarios.
     *
     * @return The preference's index.
     */
    public abstract int getIndex();

    /**
     * Get the item index. This is a zero-based index for the item, used for making
     * item data easy to store in arrays.
     *
     * @return The item index.
     */
    public abstract int getItemIndex();

    /**
     * Get the user index. This is a zero-based index for the user, used for making
     * user data easy to store in arrays.
     *
     * @return The user index.
     */
    public abstract int getUserIndex();
}
