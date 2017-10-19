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
package org.grouplens.lenskit.transform.truncate;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.junit.Test;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TopNTruncatorTest {

    private static final double EPSILON = 1.0e-6;

    @Test
    public void testSimpleTruncate() {
        long[] keys = {1, 2, 3, 4, 5};
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        Long2DoubleSortedArrayMap v = Long2DoubleSortedArrayMap.wrapUnsorted(keys, values);

        VectorTruncator truncator = new TopNTruncator(3, null);
        Long2DoubleMap v2 = truncator.truncate(v);

        long i = 3;
        for (Long2DoubleMap.Entry e: v2.long2DoubleEntrySet()) {
            assertThat(e.getLongKey(), equalTo(i));
            assertThat(e.getDoubleValue(), closeTo(i, EPSILON));
            i++;
        }
        assertThat(i, equalTo(6L));
    }
}
