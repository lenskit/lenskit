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
package org.grouplens.lenskit.util;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.longs.LongComparators;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdBuilder;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Scored item accumulator with no upper bound.
 *
 * @author Michael Ekstrand
 */
public final class UnlimitedScoredItemAccumulator implements ScoredItemAccumulator {
    private List<ScoredId> scores;

    @Override
    public boolean isEmpty() {
        return scores == null || scores.isEmpty();
    }

    @Override
    public int size() {
        return scores == null ? 0 : scores.size();
    }

    @Override
    public void put(long item, double score) {
        if (scores == null) {
            scores = new ArrayList<ScoredId>();
        }
        ScoredId id = new ScoredIdBuilder(item, score).build();
        scores.add(id);
    }

    @Override
    public List<ScoredId> finish() {
        if (scores == null) {
            return new ArrayList<ScoredId>();
        }

        Collections.sort(scores, new Comparator<ScoredId>() {
            @Override
            public int compare(ScoredId o1, ScoredId o2) {
                return DoubleComparators.OPPOSITE_COMPARATOR.compare(o1.getScore(), o2.getScore());
            }
        });

        List<ScoredId> r = scores;
        scores = null;
        return r;
    }

    @Override
    public MutableSparseVector vectorFinish() {
        if (scores == null) {
            return new MutableSparseVector();
        }

        long[] keys = new long[scores.size()];
        double [] values = new double[scores.size()];
        int i = 0;
        for (ScoredId id : scores) {
            keys[i] = id.getId();
            values[i] = id.getScore();
            i++;
        }
        scores = null;

        IdComparator comparator = new IdComparator(keys);
        ParallelSwapper swapper = new ParallelSwapper(keys, values);
        Arrays.quickSort(0, keys.length, comparator, swapper);

        return MutableSparseVector.wrap(keys, values);

    }

    private static class IdComparator extends AbstractIntComparator {

        private long[] keys;

        public IdComparator(long[] keys) {
            this.keys = keys;
        }

        @Override
        public int compare(int i, int i2) {
            return LongComparators.NATURAL_COMPARATOR.compare(keys[i], keys[i2]);
        }
    }

    private static class ParallelSwapper implements Swapper {

        private long[] keys;
        private double[] values;

        public ParallelSwapper(long[] keys, double[] values) {
            this.keys = keys;
            this.values = values;
        }

        @Override
        public void swap(int i, int i2) {
            long lTemp = keys[i];
            keys[i] = keys[i2];
            keys[i2] = lTemp;

            double dTemp = values[i];
            values[i] = values[i2];
            values[i2] = dTemp;
        }
    }
}