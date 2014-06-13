/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import org.apache.commons.lang3.builder.Builder;

/**
 * Build an indexed preference.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 * @compat Public
 */
public final class IndexedPreferenceBuilder implements Builder<IndexedPreference> {
    private int index;
    private long user;
    private int userIndex;
    private long item;
    private int itemIndex;
    private double value;

    /**
     * Create a new preference builder initialized as a copy of a preference.
     * @param pref The preference to copy.
     * @return A new builder initialized with a copy of {@var pref}'s data.
     */
    public static IndexedPreferenceBuilder copy(IndexedPreference pref) {
        return new IndexedPreferenceBuilder()
                .setIndex(pref.getIndex())
                .setUserId(pref.getUserId())
                .setUserIndex(pref.getUserIndex())
                .setItemId(pref.getItemId())
                .setItemIndex(pref.getItemIndex())
                .setValue(pref.getValue());
    }

    /**
     * Set the preference's index.
     * @param i The index.
     * @return The preference builder (for chaining).
     */
    public IndexedPreferenceBuilder setIndex(int i) {
        index = i;
        return this;
    }

    /**
     * Set the preference's user ID.
     * @param u The user ID.
     * @return The preference builder (for chaining).
     */
    public IndexedPreferenceBuilder setUserId(long u) {
        user = u;
        return this;
    }

    /**
     * Set the preference's user index.
     * @param uidx The user index.
     * @return The preference builder (for chaining).
     */
    public IndexedPreferenceBuilder setUserIndex(int uidx) {
        userIndex = uidx;
        return this;
    }

    /**
     * Set the preference's item ID.
     * @param i The item ID.
     * @return The preference builder (for chaining).
     */
    public IndexedPreferenceBuilder setItemId(long i) {
        item = i;
        return this;
    }

    /**
     * Set the preference's item index.
     * @param iidx The item index.
     * @return The preference builder (for chaining).
     */
    public IndexedPreferenceBuilder setItemIndex(int iidx) {
        itemIndex = iidx;
        return this;
    }

    /**
     * Set the preference's value.
     * @param v The preference value.
     * @return The preference builder (for chaining).
     */
    public IndexedPreferenceBuilder setValue(double v) {
        value = v;
        return this;
    }

    /**
     * Build the indexed preference.
     * @return The newly-constructed indexed preference.
     */
    @Override
    public IndexedPreference build() {
        return new SimpleIndexedPreference(user, item, value,
                                           index, userIndex, itemIndex);
    }
}
