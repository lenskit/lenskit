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

/**
 * A Preference that also provides 0-based indices for the user, item, and itself.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public abstract class IndexedPreference extends Preference {
    /**
     * Get the preference index. Each indexed preference (within a given context,
     * such as a {@link org.grouplens.lenskit.data.snapshot.PackedPreferenceSnapshot})
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

    @Override
    public IndexedPreference copy() {
        return IndexedPreferenceBuilder.copy(this).build();
    }
}
