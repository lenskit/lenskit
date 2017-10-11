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
package org.lenskit.util.math;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
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
            Long2DoubleMap m = new Long2DoubleOpenHashMap(map);
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

    @Test
    public void testAddScalar() {
        for (Map<Long,Double> map: someMaps(longs(), doubles(-1000, 1000))) {
            double scalar = doubles(-250, 250).next();
            Long2DoubleMap m = new Long2DoubleOpenHashMap(map);

            Long2DoubleMap result = Vectors.addScalar(m, scalar);
            assertThat(Vectors.sum(result), closeTo(Vectors.sum(m) + m.size() * scalar, 1.0e-6));

            for (long key: result.keySet()) {
                assertThat(result.get(key), closeTo(map.get(key) + scalar, 1.0e-6));
            }
        }
    }

    @Test
    public void testAddScalarSorted() {
        for (Map<Long,Double> map: someMaps(longs(), doubles(-1000, 1000))) {
            double scalar = doubles(-250, 250).next();
            Long2DoubleMap m = Long2DoubleSortedArrayMap.create(map);

            Long2DoubleMap result = Vectors.addScalar(m, scalar);
            assertThat(Vectors.sum(result), closeTo(Vectors.sum(m) + m.size() * scalar, 1.0e-6));

            for (long key: result.keySet()) {
                assertThat(result.get(key), closeTo(map.get(key) + scalar, 1.0e-6));
            }
        }
    }

    @Test
    public void testMultiplyScalar() {
        assertThat(Vectors.multiplyScalar(EMPTY_MAP, 2).size(),
                   equalTo(0));
        Long2DoubleMap result;

        result = Vectors.multiplyScalar(singleton(42, 3), 2);
        assertThat(result.keySet(), contains(42L));
        assertThat(result, hasEntry(42L, 6.0));

        Long2DoubleMap input = new Long2DoubleOpenHashMap();
        input.put(42L, 5);
        input.put(22L, -2);
        result = Vectors.multiplyScalar(input, -1);
        assertThat(result.keySet(), containsInAnyOrder(42L, 22L));
        assertThat(result, hasEntry(42L, -5.0));
        assertThat(result, hasEntry(22L, 2.0));
    }

    @Test
    public void testCombine() {
        Long2DoubleMap x = new Long2DoubleOpenHashMap();
        x.put(4L, 3.5);
        x.put(5L, 4.0);
        Long2DoubleMap y = Long2DoubleMaps.singleton(4L, 0.5);

        Long2DoubleMap res = Vectors.combine(x, y, 2, 1);
        assertThat(res.get(4L), closeTo(5.5, 1e-6));
        assertThat(res.get(5L), closeTo(5.0, 1e-6));
        assertThat(res.get(1L), equalTo(0.0));
    }

    @Test
    public void testCombineSorted() {
        Long2DoubleMap x = new Long2DoubleOpenHashMap();
        x.put(4L, 3.5);
        x.put(5L, 4.0);
        Long2DoubleMap y = Long2DoubleMaps.singleton(4L, 0.5);

        Long2DoubleMap res = Vectors.combine(Long2DoubleSortedArrayMap.create(x), y, 2, 1);
        assertThat(res.get(4L), closeTo(5.5, 1e-6));
        assertThat(res.get(5L), closeTo(5.0, 1e-6));
        assertThat(res.get(1L), equalTo(0.0));
    }

    @Test
    public void testUnitEmpty() {
        Long2DoubleMap unit = Vectors.unitVector(Long2DoubleMaps.EMPTY_MAP);
        assertThat(unit.size(), equalTo(0));
    }

    @Test
    public void testUnitSingletonUnit() {
        Long2DoubleMap unit = Vectors.unitVector(Long2DoubleMaps.singleton(42L, 1.0));
        assertThat(unit.size(), equalTo(1));
        assertThat(unit, hasEntry(equalTo(42L), closeTo(1.0, 1.0e-6)));
    }

    @Test
    public void testUnitSingleton() {
        Long2DoubleMap unit = Vectors.unitVector(Long2DoubleMaps.singleton(42L, 5.0));
        assertThat(unit.size(), equalTo(1));
        assertThat(unit, hasEntry(equalTo(42L), closeTo(1.0, 1.0e-6)));
    }

    @Test
    public void testUnitVector() {
        for (Map<Long,Double> map: someMaps(longs(), doubles(-100, 100))) {
            if (map.isEmpty()) {
                continue;
            }

            Long2DoubleMap vec = LongUtils.frozenMap(map);
            double norm = Vectors.euclideanNorm(vec);

            Long2DoubleMap unit = Vectors.unitVector(vec);
            assertThat(unit.size(), equalTo(vec.size()));
            assertThat(unit.keySet(), equalTo(vec.keySet()));
            assertThat(Vectors.euclideanNorm(unit),
                       closeTo(1.0, 1.0e-6));
            Long2DoubleMaps.fastForEach(unit, e -> {
                assertThat(e.getDoubleValue() * norm,
                           closeTo(vec.get(e.getLongKey()), 1.0e-6));
            });
        }
    }
}
