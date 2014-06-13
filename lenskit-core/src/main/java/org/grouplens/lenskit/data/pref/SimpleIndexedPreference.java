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

import javax.annotation.concurrent.Immutable;

/**
 * Basic indexed preference implementation that stores data in fields.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
@Immutable
class SimpleIndexedPreference extends SimplePreference implements IndexedPreference {
    private final int index;
    private final int userIndex;
    private final int itemIndex;

    /**
     * Construct a new indexed preference object.
     *
     * @param uid  The user ID.
     * @param iid  The item ID.
     * @param v    The preference value.
     * @param idx  The preference index
     * @param uidx The user index
     * @param iidx The item index
     */
    public SimpleIndexedPreference(long uid, long iid, double v,
                                   int idx, int uidx, int iidx) {
        super(uid, iid, v);
        index = idx;
        userIndex = uidx;
        itemIndex = iidx;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getUserIndex() {
        return userIndex;
    }

    @Override
    public int getItemIndex() {
        return itemIndex;
    }
}
