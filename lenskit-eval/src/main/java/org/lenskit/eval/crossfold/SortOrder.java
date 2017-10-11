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
package org.lenskit.eval.crossfold;

import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Sort order for partitioning events per user or item.
 */
public enum SortOrder {
    RANDOM {
        @Override
        public void apply(List<? extends Rating> list, Random rng) {
            Collections.shuffle(list, rng);
        }
    },
    TIMESTAMP {
        @Override
        public void apply(List<? extends Rating> list, Random rng) {
            Collections.sort(list, Ratings.TIMESTAMP_COMPARATOR);
        }
    };

    /**
     * Apply the ordering.
     *
     * @param list The list to order.
     * @param rng  The random number generator to use, if necessary.
     */
    public abstract void apply(List<? extends Rating> list, Random rng);

    public static SortOrder fromString(String str) {
        switch (str.toLowerCase()) {
        case "random":
            return RANDOM;
        case "timestamp":
            return TIMESTAMP;
        default:
            throw new IllegalArgumentException("invalid sort order " + str);
        }
    }
}
