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
