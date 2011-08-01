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
package org.grouplens.lenskit.data.pref;

import org.grouplens.lenskit.data.snapshot.RatingSnapshot;

/**
 * Indexed preference - a preference where user and item IDs are associated with
 * contiguous 0-based indices. It is used in cases where arrays of item and/or
 * user data are being computed; {@link RatingSnapshot} makes indexed
 * preferences available to allow recommender implementations to be more
 * efficient.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public class MutableIndexedPreference extends MutablePreference implements IndexedPreference {
    int index, userIndex, itemIndex;

    /**
     * Create a new indexed preference.
     * 
     * @param uid The user ID.
     * @param iid The item ID.
     * @param r The preference value.
     * @param uidx The user index.
     * @param iidx The item index.
     */
    public MutableIndexedPreference(long uid, long iid, double r, int idx, int uidx, int iidx) {
        super(uid, iid, r);
        index = idx;
        userIndex = uidx;
        itemIndex = iidx;
    }

    /**
     * Create a new, all-zero preference.
     */
    public MutableIndexedPreference() {
        this(0, 0, 0, 0, 0, 0);
    }
    
    @Override
    public final int getIndex() {
        return index;
    }

    @Override
    public final int getItemIndex() {
        return itemIndex;
    }

    @Override
    public final int getUserIndex() {
        return userIndex;
    }

    public final void setItemIndex(int idx) {
        itemIndex = idx;
    }

    public final void setUserIndex(int idx) {
        userIndex = idx;
    }
}
