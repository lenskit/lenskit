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
package org.lenskit.knn.user;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import java.util.Comparator;

/**
 * Representation of a single neighboring user.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class Neighbor {
    public final long user;
    public final Long2DoubleMap vector;
    public final double similarity;

    /**
     * Construct a new neighbor.
     * @param u   The neighbor's ID.
     * @param v   The neighbor's normalized rating vector.
     * @param sim The neighbor's similarity to the query user.
     */
    public Neighbor(long u, Long2DoubleMap v, double sim) {
        user = u;
        vector = v;
        similarity = sim;
    }

    /**
     * Comparator to order neighbors by similarity.
     */
    public static final Comparator<Neighbor> SIMILARITY_COMPARATOR = new Comparator<Neighbor>() {
        @Override
        public int compare(Neighbor n1, Neighbor n2) {
            return Double.compare(n1.similarity, n2.similarity);
        }
    };
}
