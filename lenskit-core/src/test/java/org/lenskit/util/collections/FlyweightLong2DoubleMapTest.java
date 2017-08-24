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