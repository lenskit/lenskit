/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.slopeone;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.util.keys.KeyedObject;
import org.lenskit.util.keys.KeyedObjectMap;
import org.lenskit.util.keys.SortedKeyIndex;

import java.io.Serializable;

/**
 * A model for a {@link SlopeOneItemScorer} or {@link WeightedSlopeOneItemScorer}.
 * Stores calculated deviation values and number of co-rating users for each item pair.
 */
@DefaultProvider(SlopeOneModelProvider.class)
@Shareable
public class SlopeOneModel implements Serializable {
    private static final long serialVersionUID = 2L;

    private final KeyedObjectMap<ModelRow> matrix;

    public SlopeOneModel(KeyedObjectMap<ModelRow> matrix) {
        this.matrix = matrix;
    }

    public double getDeviation(long item1, long item2) {
        if (item1 == item2) {
            return 0;
        } else if (item1 < item2) {
            ModelRow row = matrix.get(item1);
            if (row == null) {
                return Double.NaN;
            } else {
                return row.getDeviation(item2);
            }
        } else {
            ModelRow row = matrix.get(item2);
            if (row == null) {
                return Double.NaN;
            } else {
                return -row.getDeviation(item1);
            }
        }
    }

    public int getCoratings(long item1, long item2) {
        if (item1 == item2) {
            return 0;
        } else if (item1 < item2) {
            ModelRow row = matrix.get(item1);
            if (row == null) {
                return 0;
            } else {
                return row.getCoratings(item2);
            }
        } else {
            ModelRow row = matrix.get(item2);
            if (row == null) {
                return 0;
            } else {
                return row.getCoratings(item1);
            }
        }
    }

    static class ModelRow implements Serializable, KeyedObject {
        private static final long serialVersionUID = 1L;

        private final long item;
        private final SortedKeyIndex items;
        private final double[] deviations;
        private final int[] coratings;

        ModelRow(long i, SortedKeyIndex is, double[] ds, int[] crs) {
            assert ds.length == is.size();
            assert crs.length == is.size();
            item = i;
            items = is;
            deviations = ds;
            coratings = crs;
        }

        @Override
        public long getKey() {
            return item;
        }

        double getDeviation(long item) {
            int idx = items.tryGetIndex(item);
            if (idx >= 0) {
                return deviations[idx];
            } else {
                return Double.NaN;
            }
        }

        int getCoratings(long item) {
            int idx = items.tryGetIndex(item);
            if (idx >= 0) {
                return coratings[idx];
            } else {
                return 0;
            }
        }
    }
}
