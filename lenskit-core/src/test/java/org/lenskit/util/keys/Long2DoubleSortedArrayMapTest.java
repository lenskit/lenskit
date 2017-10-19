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
package org.lenskit.util.keys;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.lenskit.util.collections.LongUtils;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someMaps;
import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someSets;
import static net.java.quickcheck.generator.CombinedGenerators.sets;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class Long2DoubleSortedArrayMapTest {
    @Test
    public void testEmptyMap() {
        Long2DoubleSortedMap map = new Long2DoubleSortedArrayMap(SortedKeyIndex.empty(), new double[0]);
        assertThat(map.size(), equalTo(0));
        assertThat(map.isEmpty(), equalTo(true));
        assertThat(map.keySet(), hasSize(0));
        assertThat(map.entrySet(), hasSize(0));
        assertThat(map.values(), hasSize(0));
        assertThat(map.long2DoubleEntrySet(), hasSize(0));
        assertThat(map.get(42L), equalTo(0.0));
        assertThat(map.get((Long) 42L), nullValue());
        try {
            map.entrySet().first();
            fail("entrySet.first should fail");
        } catch (NoSuchElementException e) {
            /* expected */
        }
        try {
            map.entrySet().last();
            fail("entrySet.last should fail");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }

    @Test
    public void testSingletonMap() {
        Long2DoubleSortedMap map = new Long2DoubleSortedArrayMap(SortedKeyIndex.create(42),
                                                                 new double[]{3.5});
        assertThat(map.get(42L), equalTo(3.5));
        assertThat(map.size(), equalTo(1));
        assertThat(map.isEmpty(), equalTo(false));
        assertThat(map.keySet(), contains(42L));
        assertThat(map.values(), contains(3.5));
        assertThat(map.entrySet(), hasSize(1));
        assertThat(map.firstLongKey(), equalTo(42L));
        assertThat(map.lastLongKey(), equalTo(42L));
        Map.Entry<Long, Double> ent = map.entrySet().first();
        assertThat(ent, notNullValue());
        assertThat(ent.getKey(), equalTo(42L));
        assertThat(ent.getValue(), equalTo(3.5));
        assertThat(map.entrySet().contains(Pair.of(42L, 3.5)),
                   equalTo(true));
        assertThat(map.entrySet().contains(Pair.of(42L, 3.7)),
                   equalTo(false));
        assertThat(map.entrySet().contains(Pair.of(41L, 3.5)),
                   equalTo(false));
        assertThat(map.entrySet().first(), equalTo((Object) Pair.of(42L, 3.5)));
        assertThat(map.entrySet().last(), equalTo((Object) Pair.of(42L, 3.5)));
    }

    @Test
    public void testCreateWithLists() {
        for (Set<Long> keys: someSets(longs(), integers(0, 500))) {
            LongSortedSet sorted = LongUtils.packedSet(keys);
            SortedKeyIndex dom = SortedKeyIndex.fromCollection(keys);
            double[] values = new double[dom.size()];
            for (int i = 0; i < dom.size(); i++) {
                values[i] = doubles().next();
            }
            Long2DoubleSortedMap map = new Long2DoubleSortedArrayMap(dom, values);
            assertThat(map.size(), equalTo(dom.size()));

            assertThat(map.size(), equalTo(keys.size()));
            if (map.size() > 0) {
                assertThat(map.entrySet().first().getKey(), equalTo(sorted.firstLong()));
                assertThat(map.entrySet().last().getKey(), equalTo(sorted.lastLong()));
                assertThat(map.firstLongKey(), equalTo(sorted.firstLong()));
                assertThat(map.lastLongKey(), equalTo(sorted.lastLong()));
            }
            assertThat(map.keySet(), equalTo(sorted));
            for (Long k: keys) {
                assertThat(map.containsKey(k), equalTo(true));
            }
        }
    }

    @Test
    public void testSublist() {
        double[] values = { 1.5, 2.4, -3.2, 4.3, -5.7 };
        Long2DoubleSortedMap map = new Long2DoubleSortedArrayMap(SortedKeyIndex.create(1, 2, 3, 4, 5),
                                                                 values);
        assertThat(map.size(), equalTo(5));

        Long2DoubleSortedMap sub = map.subMap(2, 5);
        assertThat(sub.size(), equalTo(3));
        assertThat(sub.containsKey(2L), equalTo(true));
        assertThat(sub.containsKey(1L), equalTo(false));
        assertThat(sub.containsKey(5L), equalTo(false));
        assertThat(sub.containsKey(4L), equalTo(true));
        assertThat(sub.keySet(), contains(2L, 3L, 4L));
    }

    @Test
    public void testSubMap() {
        SortedKeyIndex idx = SortedKeyIndex.create(1, 2, 3, 4, 5);
        double[] values = { 1.5, 2.4, -3.2, 4.3, -5.7 };
        Long2DoubleSortedArrayMap map = new Long2DoubleSortedArrayMap(idx, values);
        assertThat(map.size(), equalTo(5));

        Long2DoubleSortedMap sub = map.subMap(LongUtils.packedSet(2L, 4L));
        assertThat(sub.size(), equalTo(2));
        assertThat(sub.containsKey(2L), equalTo(true));
        assertThat(sub.containsKey(1L), equalTo(false));
        assertThat(sub.containsKey(5L), equalTo(false));
        assertThat(sub.containsKey(4L), equalTo(true));
        assertThat(sub.containsKey(3L), equalTo(false));
        assertThat(sub.keySet(), contains(2L, 4L));
        assertThat(sub, hasEntry(2L, 2.4));
        assertThat(sub, hasEntry(4L, 4.3));
    }

    @Test
    public void testSubMapUnpacked() {
        SortedKeyIndex idx = SortedKeyIndex.create(1, 2, 3, 4, 5);
        double[] values = { 1.5, 2.4, -3.2, 4.3, -5.7 };
        Long2DoubleSortedArrayMap map = new Long2DoubleSortedArrayMap(idx, values);
        assertThat(map.size(), equalTo(5));

        Long2DoubleSortedMap sub = map.subMap(new LongOpenHashSet(LongUtils.packedSet(2L, 10L, 4L)));
        assertThat(sub.size(), equalTo(2));
        assertThat(sub.containsKey(2L), equalTo(true));
        assertThat(sub.containsKey(1L), equalTo(false));
        assertThat(sub.containsKey(5L), equalTo(false));
        assertThat(sub.containsKey(4L), equalTo(true));
        assertThat(sub.containsKey(3L), equalTo(false));
        assertThat(sub.keySet(), contains(2L, 4L));
        assertThat(sub, hasEntry(2L, 2.4));
        assertThat(sub, hasEntry(4L, 4.3));
    }

    @Test
    public void testRandomMaps() {
        for (Map<Long,Double> map: someMaps(longs(), doubles())) {
            Long2DoubleSortedArrayMap vec = Long2DoubleSortedArrayMap.create(map);
            Set<Long> picked = sets(map.keySet()).next();
            Set<Long> extra = sets(longs()).next();
            LongSortedSet wanted = LongUtils.setUnion(LongUtils.asLongSet(picked), LongUtils.asLongSet(extra));
            Long2DoubleSortedMap sv = vec.subMap(wanted);
            assertThat(sv.keySet(), everyItem(isIn(wanted)));
            assertThat(sv.keySet(), containsInAnyOrder(picked.toArray()));
            assertThat(sv.entrySet(), everyItem(isIn(map.entrySet())));
        }
    }

    @Test
    public void testIterStartFrom() {
        double[] values = { 1.5, 2.4, -3.2, 4.3, -5.7 };
        Long2DoubleSortedMap map = new Long2DoubleSortedArrayMap(SortedKeyIndex.create(1, 2, 3, 4, 5),
                                                                 values);

        AbstractLong2DoubleMap.BasicEntry key = new AbstractLong2DoubleMap.BasicEntry(2, 2.0);
        ObjectBidirectionalIterator<Long2DoubleMap.Entry> iter = map.long2DoubleEntrySet().iterator(key);
        assertThat(iter.next().getLongKey(), equalTo(3L));
        assertThat(iter.previous().getLongKey(), equalTo(3L));
        assertThat(iter.previous().getLongKey(), equalTo(2L));
    }

    @Test
    public void testFastIterStartFrom() {
        double[] values = { 1.5, 2.4, -3.2, 4.3, -5.7 };
        Long2DoubleSortedArrayMap map = new Long2DoubleSortedArrayMap(SortedKeyIndex.create(1, 2, 3, 4, 5),
                                                                      values);

        AbstractLong2DoubleMap.BasicEntry key = new AbstractLong2DoubleMap.BasicEntry(2, 2.0);
        ObjectBidirectionalIterator<Long2DoubleMap.Entry> iter = map.long2DoubleEntrySet().fastIterator(key);
        assertThat(iter.next().getLongKey(), equalTo(3L));
        assertThat(iter.previous().getLongKey(), equalTo(3L));
        assertThat(iter.previous().getLongKey(), equalTo(2L));
    }

    @Test
    public void testFromArray() {
        HashKeyIndex map = new HashKeyIndex();
        map.internId(42);
        map.internId(37);
        map.internId(62);
        double[] values = { 3.5, 4.9, 1.8 };
        Long2DoubleMap res = Long2DoubleSortedArrayMap.fromArray(map, values);
        assertThat(res.size(), equalTo(3));
        assertThat(res.keySet(), contains(37L, 42L, 62L));
        assertThat(res, hasEntry(37L, 4.9));
        assertThat(res, hasEntry(42L, 3.5));
        assertThat(res, hasEntry(62L, 1.8));
    }

    @Test
    public void testSubMapIndexes() {
        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        map.put(1, 1.0);
        map.put(2, 2.0);
        map.put(3, 3.0);
        map.put(4, 4.0);
        Long2DoubleSortedArrayMap sam = Long2DoubleSortedArrayMap.create(map);
        Long2DoubleSortedArrayMap s2 = sam.subMap(2, 4);
        assertThat(s2.keySet(), contains(2L, 3L));
        assertThat(s2.getKeyByIndex(0), equalTo(2L));
        assertThat(s2.getKeyByIndex(1), equalTo(3L));
        assertThat(s2.getValueByIndex(0), equalTo(2.0));
        assertThat(s2.getValueByIndex(1), equalTo(3.0));
    }
}
