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
package org.lenskit.transform.normalize;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.math.Vectors;

import static java.lang.Math.sqrt;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UnitVectorNormalizerTest {
    UnitVectorNormalizer norm = new UnitVectorNormalizer();
    LongSortedSet keySet;

    @Before
    public void createKeySet() {
        keySet = LongUtils.packedSet(1, 3, 4, 6);
    }

    @Test
    public void testMapVector() {
        Long2DoubleMap v = new Long2DoubleOpenHashMap();
        v.put(1L, 1.0);
        v.put(4L, 1.0);
        Long2DoubleMap ref = new Long2DoubleOpenHashMap();
        ref.put(1L, 1.0);
        ref.put(6L, 1.0);
        ref.put(3L, 2.0);

        InvertibleFunction<Long2DoubleMap, Long2DoubleMap> tx = norm.makeTransformation(ref);
        Long2DoubleMap out = tx.apply(v);

        assertThat(Vectors.euclideanNorm(out), closeTo(sqrt(2.0 / 6), 1.0e-6));
        assertThat(out.size(), equalTo(2));
        assertThat(out.get(1), closeTo(1 / sqrt(6), 1.0e-6));
        assertThat(out.get(4), closeTo(1 / sqrt(6), 1.0e-6));

        out = tx.unapply(out);
        assertThat(out.size(), equalTo(2));
        assertThat(out.get(1), closeTo(1, 1.0e-6));
        assertThat(out.get(4), closeTo(1, 1.0e-6));
        assertThat(Vectors.sum(out), closeTo(2, 1.0e-6));
        assertThat(Vectors.euclideanNorm(out), closeTo(sqrt(2), 1.0e-6));
    }

    @Test
    public void testScale() {
        Long2DoubleMap v = new Long2DoubleOpenHashMap();
        v.put(1, 1);
        v.put(4, 1);
        Long2DoubleMap v2 = norm.makeTransformation(v).apply(v);
        assertThat(Vectors.euclideanNorm(v2), closeTo(1, 1.0e-6));
        assertThat(v2.size(), equalTo(2));
        assertThat(v2.get(1), closeTo(1 / sqrt(2), 1.0e-6));
        assertThat(v2.get(4), closeTo(1 / sqrt(2), 1.0e-6));
    }
}
