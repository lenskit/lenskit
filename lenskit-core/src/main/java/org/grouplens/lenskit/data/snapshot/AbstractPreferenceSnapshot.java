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
package org.grouplens.lenskit.data.snapshot;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.pref.Preferences;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * Base class for implementing preference snapshots.
 */
public abstract class AbstractPreferenceSnapshot implements PreferenceSnapshot {
    /**
     * The user vector cache.
     */
    protected Long2ObjectMap<SparseVector> cache;

    /**
     * Initialize the snapshot.
     */
    public AbstractPreferenceSnapshot() {
        cache = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<SparseVector>());
    }

    @Override
    public SparseVector userRatingVector(long userId) {
        // FIXME Don't make this so locky
        synchronized (cache) {
            SparseVector data = cache.get(userId);
            if (data != null) {
                return data;
            } else {
                FastCollection<IndexedPreference> prefs = this.getUserRatings(userId);
                data = Preferences.userPreferenceVector(prefs).freeze();
                cache.put(userId, data);
                return data;
            }
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() {
        cache = null;
    }
}
