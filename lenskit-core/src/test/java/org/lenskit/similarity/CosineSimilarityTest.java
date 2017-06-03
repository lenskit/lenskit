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
package org.lenskit.similarity;


import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import static org.junit.Assert.*;

public class CosineSimilarityTest {
    private static final double EPSILON = 1.0e-6;
    private CosineVectorSimilarity similarity, dampedSimilarity;

    @Before
    public void setUp() throws Exception {
        similarity = new CosineVectorSimilarity();
        dampedSimilarity = new CosineVectorSimilarity(10);
    }

    private Long2DoubleMap emptyVector() {
        return Long2DoubleMaps.EMPTY_MAP;
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
        Long2DoubleMap v1, v2;
        v1 = Long2DoubleSortedArrayMap.wrapUnsorted(k1, val1);
        v2 = Long2DoubleSortedArrayMap.wrapUnsorted(k2, val2);
        assertEquals(0, similarity.similarity(v1, v2), EPSILON);
        assertEquals(0, dampedSimilarity.similarity(v1, v2), EPSILON);
    }

    @Test
    public void testEqualKeys() {
        long[] keys = {2, 5, 6};
        double[] val1 = {1, 2, 1};
        double[] val2 = {1, 2, 5};
        Long2DoubleMap v1 = Long2DoubleSortedArrayMap.wrapUnsorted(keys, val1);
        Long2DoubleMap v2 = Long2DoubleSortedArrayMap.wrapUnsorted(keys, val2);
        assertEquals(1, similarity.similarity(v1, v1), EPSILON);
        assertEquals(0.745355993, similarity.similarity(v1, v2), EPSILON);
    }

    @Test
    public void testDampedEqualKeys() {
        long[] keys = {2, 5, 6};
        double[] val1 = {1, 2, 1};
        double[] val2 = {1, 2, 5};
        Long2DoubleMap v1 = Long2DoubleSortedArrayMap.wrapUnsorted(keys, val1);
        Long2DoubleMap v2 = Long2DoubleSortedArrayMap.wrapUnsorted(keys, val2);
        assertEquals(0.375, dampedSimilarity.similarity(v1, v1), EPSILON);
        assertEquals(0.42705098, dampedSimilarity.similarity(v1, v2), EPSILON);
    }

    @Test
    public void testOverlap() {
        long[] k1 = {1, 2, 5, 6};
        double[] val1 = {3, 1, 2, 1};
        long[] k2 = {2, 3, 5, 6, 7};
        double[] val2 = {1, 7, 2, 5, 0};
        Long2DoubleMap v1 = Long2DoubleSortedArrayMap.wrapUnsorted(k1, val1);
        Long2DoubleMap v2 = Long2DoubleSortedArrayMap.wrapUnsorted(k2, val2);
        assertEquals(1, similarity.similarity(v1, v1), EPSILON);
        assertEquals(1, similarity.similarity(v2, v2), EPSILON);
        assertEquals(0.29049645, similarity.similarity(v1, v2), EPSILON);
    }
}
