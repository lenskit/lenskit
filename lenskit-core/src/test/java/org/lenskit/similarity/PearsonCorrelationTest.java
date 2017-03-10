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
