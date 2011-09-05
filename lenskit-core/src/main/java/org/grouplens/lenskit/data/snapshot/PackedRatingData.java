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
package org.grouplens.lenskit.data.snapshot;

import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.pref.MutableIndexedPreference;

/**
 * Data storage for packed build contexts.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
final class PackedRatingData {
    final int[] users;
    final int[] items;
    final double[] values;
    final Index itemIndex;
    final Index userIndex;

    PackedRatingData(int[] users, int[] items, double[] values, Index userIndex, Index itemIndex) {
        this.users = users;
        this.items = items;
        this.values = values;
        this.userIndex = userIndex;
        this.itemIndex = itemIndex;
    }

    public IndirectPreference makeRating(int index) {
        return new IndirectPreference(index);
    }

    final class IndirectPreference implements IndexedPreference {
        int index;
        IndirectPreference() {
            this(-1);
        }

        IndirectPreference(int idx) {
            index = idx;
        }

        @Override
        public long getUserId() {
            return userIndex.getId(users[index]);
        }

        @Override
        public long getItemId() {
            return itemIndex.getId(items[index]);
        }

        @Override
        public double getValue() {
            return values[index];
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public int getUserIndex() {
            return users[index];
        }

        @Override
        public int getItemIndex() {
            return items[index];
        }

        @Override
        public IndexedPreference clone() {
            return new MutableIndexedPreference(getUserId(), getItemId(),
                                                getValue(), getIndex(),
                                                getUserIndex(), getItemIndex());
        }
    }
}
