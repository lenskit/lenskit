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
package org.grouplens.lenskit.knn;


import static org.junit.Assert.assertEquals;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.junit.Before;
import org.junit.Test;

public class TestCosineSimilarity {
    private static final double EPSILON = 1.0e-6;
    private CosineSimilarity similarity, dampedSimilarity;

    @Before
    public void setUp() throws Exception {
        similarity = new CosineSimilarity();
        dampedSimilarity = new CosineSimilarity(10);
    }

    private SparseVector emptyVector() {
        long[] keys = {};
        double[] values = {};
        return SparseVector.wrap(keys, values);
    }

    @Test
    public void testEmpty() {
        assertEquals(0, similarity.similarity(emptyVector(), emptyVector()), EPSILON);
    }

    @Test
    public void testEmptyDamped() {
        assertEquals(0, dampedSimilarity.similarity(emptyVector(), emptyVector()), EPSILON);
    }

    @Test
    public void testDisjoint() {
        long[] k1 = {2, 5, 6};
        double[] val1 = {1, 3, 2};
        long[] k2 = {3, 4, 7};
        double[] val2 = {1, 3, 2};
        SparseVector v1, v2;
        v1 = SparseVector.wrap(k1,val1);
        v2 = SparseVector.wrap(k2,val2);
        assertEquals(0, similarity.similarity(v1,v2), EPSILON);
        assertEquals(0, dampedSimilarity.similarity(v1,v2), EPSILON);
    }

    @Test
    public void testEqualKeys() {
        long[] keys = {2, 5, 6};
        double[] val1 = {1, 2, 1};
        double[] val2 = {1, 2, 5};
        SparseVector v1 = SparseVector.wrap(keys, val1);
        SparseVector v2 = SparseVector.wrap(keys, val2);
        assertEquals(1, similarity.similarity(v1, v1), EPSILON);
        assertEquals(0.745355993, similarity.similarity(v1, v2), EPSILON);
    }

    @Test
    public void testDampedEqualKeys() {
        long[] keys = {2, 5, 6};
        double[] val1 = {1, 2, 1};
        double[] val2 = {1, 2, 5};
        SparseVector v1 = SparseVector.wrap(keys, val1);
        SparseVector v2 = SparseVector.wrap(keys, val2);
        assertEquals(0.375, dampedSimilarity.similarity(v1, v1), EPSILON);
        assertEquals(0.42705098, dampedSimilarity.similarity(v1, v2), EPSILON);
    }

    @Test
    public void testOverlap() {
        long[] k1 = {1, 2, 5, 6};
        double[] val1 = {3, 1, 2, 1};
        long[] k2 = {2, 3, 5, 6, 7};
        double[] val2 = {1, 7, 2, 5, 0};
        SparseVector v1 = SparseVector.wrap(k1, val1);
        SparseVector v2 = SparseVector.wrap(k2, val2);
        assertEquals(1, similarity.similarity(v1, v1), EPSILON);
        assertEquals(1, similarity.similarity(v2, v2), EPSILON);
        assertEquals(0.29049645, similarity.similarity(v1, v2), EPSILON);
    }
}
