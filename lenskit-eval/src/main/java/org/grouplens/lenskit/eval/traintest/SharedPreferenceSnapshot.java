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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Supplier;
import it.unimi.dsi.fastutil.longs.LongCollection;
import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PackedPreferenceSnapshot;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.util.Index;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.Serializable;

@Shareable
public class SharedPreferenceSnapshot implements PreferenceSnapshot, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SharedPreferenceSnapshot.class);
    private final PreferenceSnapshot snapshot;

    public SharedPreferenceSnapshot(PreferenceSnapshot snapshot) {
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
    public SparseVector userRatingVector(long userId) {
        return snapshot.userRatingVector(userId);
    }

    /**
     * Construct a supplier of a snapshot of the training data from a data set.  The snapshot is
     * cached in a soft reference, so will be released when unused for a time.
     *
     * @param data The data set.
     * @return A supplier that provides access to a shared preference snapshot.
     */
    public static Supplier<SharedPreferenceSnapshot> supplier(TTDataSet data) {
        return new CachedSupplier(data);
    }

    private static class CachedSupplier implements Supplier<SharedPreferenceSnapshot> {
        private final TTDataSet dataSet;

        public CachedSupplier(@Nonnull TTDataSet data) {
            dataSet = data;
        }

        @Override
        public SharedPreferenceSnapshot get() {
            logger.info("Loading snapshot for {}", dataSet.getName());
            StopWatch timer = new StopWatch();
            timer.start();
            DataAccessObject dao = dataSet.getTrainFactory().snapshot();
            PreferenceSnapshot snapshot;
            try {
                snapshot = PackedPreferenceSnapshot.pack(dao);
            } finally {
                dao.close();
            }
            timer.stop();
            logger.info("Rating snapshot for {} loaded in {}",
                        dataSet.getName(), timer);
            return new SharedPreferenceSnapshot(snapshot);
        }
    }
}
