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
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PearsonCorrelationTest {
    private static final double EPSILON = 1.0e-5;

    VectorSimilarity sim;

    @Before
    public void setUp() {
        sim = new PearsonCorrelation();
    }

    @Test
    public void testEmptyVector() {
        assertThat(sim.similarity(Long2DoubleMaps.EMPTY_MAP, Long2DoubleMaps.EMPTY_MAP), closeTo(0, EPSILON));
    }

    @Test
    public void testSelfSimilarity() {
        long keys[] = {1, 5, 7};
        double values[] = {1.5, 2.5, 2};
        Long2DoubleMap v = Long2DoubleSortedArrayMap.wrapUnsorted(keys, values);
        assertThat(sim.similarity(v, v), closeTo(1, EPSILON));
        assertThat(sim.similarity(v, new Long2DoubleOpenHashMap(v)), closeTo(1, EPSILON));
    }

    @Test
    public void testDisjointSimilarity() {
        long keys[] = {1, 5, 7};
        double values[] = {1.5, 2.5, 2};
        long keys2[] = {2, 4, 8};
        Long2DoubleSortedArrayMap v1 = Long2DoubleSortedArrayMap.wrapUnsorted(keys, values);
        Long2DoubleSortedArrayMap v2 = Long2DoubleSortedArrayMap.wrapUnsorted(keys2, values);
        assertThat(sim.similarity(v1, v2), closeTo(0, EPSILON));
    }

    @Test
    public void testSimilarity() {
        long k1[] = {1, 5, 7};
        double val1[] = {1.5, 2.5, 2};
        long k2[] = {1, 5, 6};
        double val2[] = {2, 2.5, 1.7};
        Long2DoubleMap v1 = Long2DoubleSortedArrayMap.wrapUnsorted(k1, val1);
        Long2DoubleMap v2 = Long2DoubleSortedArrayMap.wrapUnsorted(k2, val2);
        assertThat(sim.similarity(v1, v2), closeTo(1, EPSILON));
    }

    @Test
    public void testSimilarity2() {
        long k1[] = {1, 5, 7, 8};
        double val1[] = {1.5, 2.5, 2, 3.5};
        long k2[] = {1, 5, 7, 9};
        double val2[] = {2, 2.5, 1.7, 0.8};
        Long2DoubleMap v1 = Long2DoubleSortedArrayMap.wrapUnsorted(k1, val1);
        Long2DoubleMap v2 = Long2DoubleSortedArrayMap.wrapUnsorted(k2, val2);
        assertThat(sim.similarity(v1, v2), closeTo(0.6185896, EPSILON));
    }
}
