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
