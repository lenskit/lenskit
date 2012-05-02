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

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import java.util.Collection;

/**
 * Utility class for working with preferences.
 * @author Michael Ekstrand
 */
public class Preferences {
    /**
     * Compute a user preference vector.
     * @param prefs The user's preferences.
     * @return A vector of the preferences.
     * @throws IllegalArgumentException if the same item appears multiple times, or there are
     *                                  preferences from multiple users.
     */
    public static MutableSparseVector userPreferenceVector(Collection<? extends Preference> prefs) {
        // find keys and pre-validate data
        Long2DoubleOpenHashMap prefMap = new Long2DoubleOpenHashMap(prefs.size());
        long user = 0;
        for (Preference p: CollectionUtils.fast(prefs)) {
            final long iid = p.getItemId();
            if (prefMap.isEmpty()) {
                user = p.getUserId();
            } else if (user != p.getUserId()) {
                throw new IllegalArgumentException("multiple user IDs in pref array");
            }
            if (prefMap.containsKey(iid)) {
                throw new IllegalArgumentException("item " + iid + " occurs multiple times");
            } else {
                prefMap.put(iid, p.getValue());
            }
        }

        return new MutableSparseVector(prefMap);
    }
}
