/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
