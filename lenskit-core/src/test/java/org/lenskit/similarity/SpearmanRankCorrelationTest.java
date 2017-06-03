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
import org.junit.Test;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import static org.junit.Assert.*;
import static org.lenskit.similarity.SpearmanRankCorrelation.rank;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SpearmanRankCorrelationTest {

    @Test
    public void testRankEmpty() {
        assertTrue(rank(Long2DoubleMaps.EMPTY_MAP).isEmpty());
    }

    @Test
    public void testRankSingle() {
        Long2DoubleSortedArrayMap v = Long2DoubleSortedArrayMap.wrapUnsorted(new long[]{1}, new double[]{5});
        Long2DoubleMap r = rank(v);
        assertEquals(1, r.size());
        assertEquals(1, r.get(1), 1.0e-6);
    }

    @Test
    public void testRankSeveral() {
        long[] keys = {1, 2, 3, 4, 5};
        double[] values = {7, 2, 3, 1, 5};
        Long2DoubleMap v = Long2DoubleSortedArrayMap.wrapUnsorted(keys, values);
        Long2DoubleMap r = rank(v);
        assertEquals(5, r.size());
        assertEquals(1, r.get(1), 1.0e-6);
        assertEquals(2, r.get(5), 1.0e-6);
        assertEquals(3, r.get(3), 1.0e-6);
        assertEquals(4, r.get(2), 1.0e-6);
        assertEquals(5, r.get(4), 1.0e-6);
    }

    @Test
    public void testRankTie() {
        long[] keys = {1, 2, 3, 4, 5};
        double[] values = {7, 2, 3, 1, 3};
        Long2DoubleMap v = Long2DoubleSortedArrayMap.wrapUnsorted(keys, values);
        Long2DoubleMap r = rank(v);
        assertEquals(5, r.size());
        assertEquals(1, r.get(1), 1.0e-6);
        assertEquals(2.5, r.get(3), 1.0e-6);
        assertEquals(2.5, r.get(5), 1.0e-6);
        assertEquals(4, r.get(2), 1.0e-6);
        assertEquals(5, r.get(4), 1.0e-6);
    }

}
