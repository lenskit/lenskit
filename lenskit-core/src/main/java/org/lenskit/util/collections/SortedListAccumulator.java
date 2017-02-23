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
package org.lenskit.util.collections;

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Accumulators for sorted lists of things.
 *
 * @param <T> The type of item to accumulate.
 */
public abstract class SortedListAccumulator<T> {
    /**
     * Create a new sorted list accumulator.
     * @param n The number of results desired; negative for unlimited.
     * @return An accumulator accumulator.
     */
    public static <T extends Comparable<? super T>> SortedListAccumulator<T> decreasing(int n) {
        if (n < 0) {
            return new Unlimited<T>(Ordering.<T>natural());
        } else {
            return new TopN<T>(n, Ordering.<T>natural());
        }
    }

    /**
     * Create a new sorted list accumulator that sorts in decreasing order.
     * @param n The number of results desired; negative for unlimited.
     * @param comp The comparator; items will be picked in *decreasing* order by this comparator.
     * @return An accumulator accumulator.
     */
    public static <T> SortedListAccumulator<T> decreasing(int n, Comparator<? super T> comp) {
        if (n < 0) {
            return new Unlimited<T>(Ordering.from(comp));
        } else {
            return new TopN<T>(n, Ordering.from(comp));
        }
    }

    private SortedListAccumulator() {}

    /**
     * Add an item to the accumulator.
     * @param x The item to add.
     */
    public abstract void add(@Nonnull T x);

    /**
     * Finish accumulating and return the accumulated items.
     *
     * When this method is called, the accumulator is reset and can be used to accumulate a fresh set of items.
     *
     * @return The accumulated items.
     */
    public abstract List<T> finish();

    private static class Unlimited<T> extends SortedListAccumulator<T> {
        private final Ordering<? super T> ordering;
        List<T> items = new ArrayList<>();

        private Unlimited(Ordering<? super T> order) {
            ordering = order;
        }

        @Override
        public void add(@Nonnull T x) {
            Preconditions.checkNotNull(x, "element");
            items.add(x);
        }

        @Override
        public List<T> finish() {
            List<T> result = items;
            Collections.sort(result, ordering.reverse());
            items = new ArrayList<>();
            return result;
        }
    }

    private static class TopN<T> extends SortedListAccumulator<T> {
        private final Ordering<? super T> ordering;
        private PriorityQueue<T> results;
        private final int size;

        private TopN(int n, Ordering<? super T> order) {
            results = new PriorityQueue<>(n + 1, order);
            size = n;
            ordering = order;
        }

        @Override
        public void add(@Nonnull T x) {
            Preconditions.checkNotNull(x, "element");
            results.add(x);
            if (results.size() > size) {
                results.remove();
            }
        }

        @Override
        public List<T> finish() {
            ArrayList<T> list = new ArrayList<>(results.size());
            while (!results.isEmpty()) {
                list.add(results.remove());
            }

            // list is now least-first, because priority queue
            // also, pq is empty
            Collections.reverse(list);
            return list;
        }
    }
}
