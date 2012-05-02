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
package org.grouplens.lenskit.data.pref;

import org.apache.commons.lang3.builder.Builder;

/**
 * Build an indexed preference.
 * @author Michael Ekstrand
 * @since 0.11
 */
public final class IndexedPreferenceBuilder implements Builder<IndexedPreference> {
    private int index;
    private long user;
    private int userIndex;
    private long item;
    private int itemIndex;
    private double value;

    public static IndexedPreferenceBuilder copy(IndexedPreference pref) {
        return new IndexedPreferenceBuilder()
                .setIndex(pref.getIndex())
                .setUserId(pref.getUserId())
                .setUserIndex(pref.getUserIndex())
                .setItemId(pref.getItemId())
                .setItemIndex(pref.getItemIndex())
                .setValue(pref.getValue());
    }

    public IndexedPreferenceBuilder setIndex(int i) {
        index = i;
        return this;
    }

    public IndexedPreferenceBuilder setUserId(long u) {
        user = u;
        return this;
    }

    public IndexedPreferenceBuilder setUserIndex(int uidx) {
        userIndex = uidx;
        return this;
    }

    public IndexedPreferenceBuilder setItemId(long i) {
        item = i;
        return this;
    }

    public IndexedPreferenceBuilder setItemIndex(int iidx) {
        itemIndex = iidx;
        return this;
    }

    public IndexedPreferenceBuilder setValue(double v) {
        value = v;
        return this;
    }

    public IndexedPreference build() {
        return new SimpleIndexedPreference(user, item, value,
                                           index, userIndex, itemIndex);
    }
}
