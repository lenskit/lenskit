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
