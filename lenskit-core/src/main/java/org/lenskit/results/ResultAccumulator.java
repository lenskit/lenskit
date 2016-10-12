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
package org.lenskit.results;

import com.google.common.base.Preconditions;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Accumulator for sorted lists of results.  This class will return result lists, with the highest-scored result first.
 *
 * Create one with {@link #create(int)}.
 */
public abstract class ResultAccumulator {
    /**
     * Create a new result accumulator.
     * @param n The number of results desired; -1 for unlimited.
     * @return A result accumulator.
     */
    public static ResultAccumulator create(int n) {
        if (n < 0) {
            return new Unlimited();
        } else {
            return new TopN(n);
        }
    }

    private ResultAccumulator() {}

    /**
     * Add a result to the accumulator.
     * @param r The result to add.
     */
    public abstract void add(@Nonnull Result r);

    /**
     * Add a basic result to the accumulator.
     * @param item The item ID to add.
     * @param score The score to add.
     */
    public void add(long item, double score) {
        add(Results.create(item, score));
    }

    /**
     * Finish accumulating and return the accumulated results.
     *
     * When this method is called, the accumulator is reset and can be used to accumulate a fresh set of results.
     *
     * @return The accumulated results, in nonincreasing order of score.
     */
    public abstract ResultList finish();

    private static class Unlimited extends ResultAccumulator {
        List<Result> results = new ArrayList<>();

        @Override
        public void add(@Nonnull Result r) {
            Preconditions.checkNotNull(r, "result");
            results.add(r);
        }

        @Override
        public ResultList finish() {
            Collections.sort(results, Results.scoreOrder().reverse());
            ResultList rv = new BasicResultList(results);
            results.clear();
            return rv;
        }
    }

    private static class TopN extends ResultAccumulator {
        private PriorityQueue<Result> results;
        private final int size;

        public TopN(int n) {
            results = new PriorityQueue<>(n, Results.scoreOrder());
            size = n;
        }

        @Override
        public void add(@Nonnull Result r) {
            Preconditions.checkNotNull(r, "result");
            results.add(r);
            if (results.size() > size) {
                results.remove();
            }
        }

        @Override
        public ResultList finish() {
            ArrayList<Result> list = new ArrayList<>();
            while (!results.isEmpty()) {
                list.add(results.remove());
            }
            // list is now least-first, because priority queue
            // also, pq is empty
            Collections.reverse(list);
            return new BasicResultList(list);
        }
    }
}
