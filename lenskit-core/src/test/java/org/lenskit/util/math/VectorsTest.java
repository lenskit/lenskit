/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.util.math;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.junit.Test;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import java.util.Map;

import static it.unimi.dsi.fastutil.longs.Long2DoubleMaps.EMPTY_MAP;
import static it.unimi.dsi.fastutil.longs.Long2DoubleMaps.singleton;
import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someMaps;
import static net.java.quickcheck.generator.PrimitiveGenerators.doubles;
import static net.java.quickcheck.generator.PrimitiveGenerators.longs;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class VectorsTest {
    @Test
    public void testDotEmpty() {
        double dp = Vectors.dotProduct(EMPTY_MAP, EMPTY_MAP);
        assertThat(dp, equalTo(0.0));
        dp = Vectors.dotProduct(EMPTY_MAP, singleton(42, 3.5));
        assertThat(dp, equalTo(0.0));
        dp = Vectors.dotProduct(singleton(42, 3.5), EMPTY_MAP);
        assertThat(dp, equalTo(0.0));
    }

    @Test
    public void testDotSingleton() {
        double dp = Vectors.dotProduct(singleton(42, 3.5), singleton(42, 2.0));
        assertThat(dp, equalTo(3.5 * 2.0));
    }

    @Test
    public void testDotSingletonMismatched() {
        double dp = Vectors.dotProduct(singleton(42, 3.5), singleton(41, 2.0));
        assertThat(dp, equalTo(0.0));
    }

    @Test
    public void testDotVectors() {
        for (Map<Long,Double> map: someMaps(longs(), doubles(-1000, 1000))) {
            Long2DoubleMap m = LongUtils.asLong2DoubleMap(map);
            assertThat(Vectors.dotProduct(m, m),
                       equalTo(Vectors.sumOfSquares(m)));
            if (!m.isEmpty()) {
                long k1 = m.keySet().iterator().nextLong();
                double v1 = m.get(k1);
                Long2DoubleMap m2 = new Long2DoubleOpenHashMap(m);
                m2.remove(k1);
                long k2 = k1;
                // find a key we haven't seen yet
                while (m2.containsKey(k2) || k2 == k1) {
                    k2++;
                }
                m2.put(k2, v1);
                // and test that it is missing from the dot product
                assertThat(Vectors.dotProduct(m, m2),
                           closeTo(Vectors.sumOfSquares(m) - v1 * v1, 1.0e-6));
            }
        }
    }

    @Test
    public void testDotSortedVectors() {
        for (Map<Long,Double> map: someMaps(longs(), doubles(-1000, 1000))) {
            Long2DoubleMap m = new Long2DoubleSortedArrayMap(map);
            assertThat(Vectors.dotProduct(m, m),
                       equalTo(Vectors.sumOfSquares(m)));
            if (!m.isEmpty()) {
                long k1 = m.keySet().iterator().nextLong();
                double v1 = m.get(k1);
                Long2DoubleMap m2 = new Long2DoubleOpenHashMap(m);
                m2.remove(k1);
                long k2 = k1;
                // find a key we haven't seen yet
                while (m2.containsKey(k2) || k2 == k1) {
                    k2++;
                }
                m2.put(k2, v1);
                m2 = new Long2DoubleSortedArrayMap(m2);
                // and test that it is missing from the dot product
                assertThat(Vectors.dotProduct(m, m2),
                           closeTo(Vectors.sumOfSquares(m) - v1 * v1, 1.0e-6));
            }
        }
    }
}