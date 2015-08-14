package org.lenskit.util.keys;

import it.unimi.dsi.fastutil.longs.AbstractLong2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleSortedMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.lenskit.util.collections.LongUtils;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someSets;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class Long2DoubleSortedArrayMapTest {
    @Test
    public void testEmptyMap() {
        Long2DoubleSortedMap map = new Long2DoubleSortedArrayMap(LongKeyIndex.empty(), new double[0]);
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
        Long2DoubleSortedMap map = new Long2DoubleSortedArrayMap(LongKeyIndex.create(42),
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
            LongKeyIndex dom = LongKeyIndex.fromCollection(keys);
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
        Long2DoubleSortedMap map = new Long2DoubleSortedArrayMap(LongKeyIndex.create(1, 2, 3, 4, 5),
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
    public void testIterStartFrom() {
        double[] values = { 1.5, 2.4, -3.2, 4.3, -5.7 };
        Long2DoubleSortedMap map = new Long2DoubleSortedArrayMap(LongKeyIndex.create(1, 2, 3, 4, 5),
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
        Long2DoubleSortedArrayMap map = new Long2DoubleSortedArrayMap(LongKeyIndex.create(1, 2, 3, 4, 5),
                                                                      values);

        AbstractLong2DoubleMap.BasicEntry key = new AbstractLong2DoubleMap.BasicEntry(2, 2.0);
        ObjectBidirectionalIterator<Long2DoubleMap.Entry> iter = map.long2DoubleEntrySet().fastIterator(key);
        assertThat(iter.next().getLongKey(), equalTo(3L));
        assertThat(iter.previous().getLongKey(), equalTo(3L));
        assertThat(iter.previous().getLongKey(), equalTo(2L));
    }
}
