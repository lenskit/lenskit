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
package org.grouplens.lenskit.knn.user;

import org.grouplens.lenskit.vectors.SparseVector;

import java.util.Comparator;

/**
 * Representation of a single neighboring user.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class Neighbor {
    public final long user;
    public final SparseVector vector;
    public final double similarity;

    /**
     * Construct a new neighbor.
     *
     * @param u   The neighbor's ID.
     * @param v   The neighbor's unnormalized rating vector.
     * @param sim The neighbor's similarity to the query user.
     */
    public Neighbor(long u, SparseVector v, double sim) {
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
