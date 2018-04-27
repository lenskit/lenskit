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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.junit.Test;

import java.util.Set;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someSets;
import static net.java.quickcheck.generator.PrimitiveGenerators.longs;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by michaelekstrand on 7/3/2017.
 */
public class FlyweightLong2DoubleMapTest {
    @Test
    public void testEmptyMap() {
        Long2DoubleMap map = LongUtils.flyweightMap(LongSets.EMPTY_SET, x -> x);
        assertThat(map.keySet(), hasSize(0));
        assertThat(map.size(), equalTo(0));
        assertThat(map.isEmpty(), equalTo(true));
        assertThat(map.getOrDefault(10, Math.PI),
                   equalTo(Math.PI));
    }

    @Test
    public void testSingletonMap() {
        Long2DoubleMap map = LongUtils.flyweightMap(LongSets.singleton(42L), x -> x);
        assertThat(map.keySet(), hasSize(1));
        assertThat(map.keySet(), contains(42L));
        assertThat(map.size(), equalTo(1));
        assertThat(map.isEmpty(), equalTo(false));
        assertThat(map, hasEntry(42L, 42.0));
        assertThat(map.getOrDefault(10, Math.PI),
                   equalTo(Math.PI));
        assertThat(map.get(10), equalTo(0.0));
    }

    @Test
    public void testMapSomeStuff() {
        for (Set<Long> keys: someSets(longs())) {
            Long2DoubleMap map = LongUtils.flyweightMap(LongUtils.frozenSet(keys),
                                                        x -> (-x) % 10);
            assertThat(map.keySet(), equalTo(keys));
            assertThat(map.size(), equalTo(keys.size()));
            for (Long k: keys) {
                assertThat(map, hasEntry(k, (double) ((-k) % 10)));
            }
        }
    }
}