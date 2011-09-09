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
package org.grouplens.lenskit.eval;

import it.unimi.dsi.fastutil.longs.LongCollection;

import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.RatingSnapshot;
import org.grouplens.lenskit.util.Index;

public class SharedRatingSnapshot implements RatingSnapshot {
    private final RatingSnapshot snapshot;

    public SharedRatingSnapshot(RatingSnapshot snapshot) {
        super();
        this.snapshot = snapshot;
    }

    @Override
    public LongCollection getUserIds() {
        return snapshot.getUserIds();
    }

    @Override
    public LongCollection getItemIds() {
        return snapshot.getItemIds();
    }

    @Override
    public Index userIndex() {
        return snapshot.userIndex();
    }

    @Override
    public Index itemIndex() {
        return snapshot.itemIndex();
    }

    @Override
    public FastCollection<IndexedPreference> getRatings() {
        return snapshot.getRatings();
    }

    @Override
    public FastCollection<IndexedPreference> getUserRatings(long userId) {
        return snapshot.getUserRatings(userId);
    }

    @Override
    public void close() {
        // don't close
    }

    @Override
    public UserVector userRatingVector(long userId) {
        return snapshot.userRatingVector(userId);
    }
}
