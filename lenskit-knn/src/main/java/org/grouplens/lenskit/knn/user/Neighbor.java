/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import java.util.Comparator;

import org.grouplens.lenskit.data.vector.MutableSparseVector;

/**
 * Representation of a single neighboring user.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class Neighbor {
    public final long userId;
    public final MutableSparseVector ratings;
    public final double similarity;
    
    /**
     * Construct a new neighbor.
     * @param user The user ID of the neighbor.
     * @param rv The neighbor's ratings vector.
     * @param sim The neighbor's similarity to the query user.
     */
    public Neighbor(long user, MutableSparseVector rv, double sim) {
        userId = user;
        ratings = rv;
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
