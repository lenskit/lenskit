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
