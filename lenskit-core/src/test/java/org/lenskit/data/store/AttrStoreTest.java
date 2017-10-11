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
package org.lenskit.data.store;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.java.quickcheck.generator.CombinedGenerators.nullsAnd;
import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someLists;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AttrStoreTest {
    @Test
    public void testEmpty() {
        AttrStoreBuilder asb = new AttrStoreBuilder();
        assertThat(asb.size(), equalTo(0));

        AttrStore store = asb.build();
        assertThat(store.size(), equalTo(0));
    }

    @Test
    public void testAddObject() {
        AttrStoreBuilder asb = new AttrStoreBuilder();
        asb.add("heffalump");
        assertThat(asb.size(), equalTo(1));
        assertThat(asb.get(0), equalTo("heffalump"));

        AttrStore store = asb.build();
        assertThat(store.size(), equalTo(1));
        assertThat(store.get(0), equalTo("heffalump"));
    }

    @Test
    public void testAddTwoObject() {
        AttrStoreBuilder asb = new AttrStoreBuilder();
        asb.add("heffalump");
        asb.add("woozle");
        assertThat(asb.size(), equalTo(2));
        assertThat(asb.get(0), equalTo("heffalump"));
        assertThat(asb.get(1), equalTo("woozle"));

        AttrStore store = asb.build();
        assertThat(store.size(), equalTo(2));
        assertThat(store.get(0), equalTo("heffalump"));
        assertThat(store.get(1), equalTo("woozle"));
    }

    @Test
    public void testSkipAdd() {
        AttrStoreBuilder asb = new AttrStoreBuilder();
        asb.add("heffalump");
        asb.skip();
        asb.add("woozle");
        assertThat(asb.size(), equalTo(3));
        assertThat(asb.get(0), equalTo("heffalump"));
        assertThat(asb.get(1), nullValue());
        assertThat(asb.get(2), equalTo("woozle"));

        AttrStore store = asb.build();
        assertThat(store.size(), equalTo(3));
        assertThat(store.get(0), equalTo("heffalump"));
        assertThat(store.get(1), nullValue());
        assertThat(store.get(2), equalTo("woozle"));
    }

    @Test
    public void testAddABunchOfStrings() {
        for (List<String> strings: someLists(nullsAnd(strings(), 20),
                                             integers(2 * Shard.SHARD_SIZE + 20, 10 * Shard.SHARD_SIZE))) {
            AttrStoreBuilder asb = new AttrStoreBuilder();
            strings.forEach(asb::add);
            assertThat(asb.size(), equalTo(strings.size()));
            AttrStore store = asb.build();
            assertThat(store.size(), equalTo(strings.size()));
            assertThat(IntStream.range(0, strings.size())
                                .mapToObj(store::get)
                                .collect(Collectors.toList()),
                       contains(strings.toArray()));
        }
    }

    @Test
    public void testAddABunchOfInts() {
        for (List<Integer> strings: someLists(nullsAnd(integers(), 20),
                                              integers(2 * Shard.SHARD_SIZE + 20, 10 * Shard.SHARD_SIZE))) {
            AttrStoreBuilder asb = new AttrStoreBuilder(IntShard::create);
            strings.forEach(asb::add);
            assertThat(asb.size(), equalTo(strings.size()));
            AttrStore store = asb.build();
            assertThat(store.size(), equalTo(strings.size()));
            assertThat(IntStream.range(0, strings.size())
                                .mapToObj(store::get)
                                .collect(Collectors.toList()),
                       contains(strings.toArray()));
        }
    }

    @Test
    public void testAddABunchOfSmallInts() {
        for (List<Integer> strings: someLists(nullsAnd(integers(Short.MIN_VALUE, Short.MAX_VALUE), 20),
                                              integers(2 * Shard.SHARD_SIZE + 20, 10 * Shard.SHARD_SIZE))) {
            AttrStoreBuilder asb = new AttrStoreBuilder(IntShard::create);
            strings.forEach(asb::add);
            assertThat(asb.size(), equalTo(strings.size()));
            AttrStore store = asb.build();
            assertThat(store.size(), equalTo(strings.size()));
            assertThat(IntStream.range(0, strings.size())
                                .mapToObj(store::get)
                                .collect(Collectors.toList()),
                       contains(strings.toArray()));
        }
    }

    @Test
    public void testAddABunchOfLongs() {
        for (List<Long> strings: someLists(nullsAnd(longs(), 20),
                                              integers(2 * Shard.SHARD_SIZE + 20, 10 * Shard.SHARD_SIZE))) {
            AttrStoreBuilder asb = new AttrStoreBuilder(LongShard::create);
            strings.forEach(asb::add);
            assertThat(asb.size(), equalTo(strings.size()));
            AttrStore store = asb.build();
            assertThat(store.size(), equalTo(strings.size()));
            assertThat(IntStream.range(0, strings.size())
                                .mapToObj(store::get)
                                .collect(Collectors.toList()),
                       contains(strings.toArray()));
        }
    }

    @Test
    public void testAddABunchOfSmallLongs() {
        for (List<Long> strings: someLists(nullsAnd(longs(Short.MIN_VALUE, Short.MAX_VALUE), 20),
                                              integers(2 * Shard.SHARD_SIZE + 20, 10 * Shard.SHARD_SIZE))) {
            AttrStoreBuilder asb = new AttrStoreBuilder(LongShard::create);
            strings.forEach(asb::add);
            assertThat(asb.size(), equalTo(strings.size()));
            AttrStore store = asb.build();
            assertThat(store.size(), equalTo(strings.size()));
            assertThat(IntStream.range(0, strings.size())
                                .mapToObj(store::get)
                                .collect(Collectors.toList()),
                       contains(strings.toArray()));
        }
    }

    @Test
    public void testAddABunchOfMediumLongs() {
        for (List<Long> strings: someLists(nullsAnd(longs(Integer.MIN_VALUE, Integer.MAX_VALUE), 20),
                                           integers(2 * Shard.SHARD_SIZE + 20, 10 * Shard.SHARD_SIZE))) {
            AttrStoreBuilder asb = new AttrStoreBuilder(LongShard::create);
            strings.forEach(asb::add);
            assertThat(asb.size(), equalTo(strings.size()));
            AttrStore store = asb.build();
            assertThat(store.size(), equalTo(strings.size()));
            assertThat(IntStream.range(0, strings.size())
                                .mapToObj(store::get)
                                .collect(Collectors.toList()),
                       contains(strings.toArray()));
        }
    }

    @Test
    public void testAddABunchOfDoubles() {
        for (List<Double> strings: someLists(nullsAnd(doubles(), 20),
                                             integers(2 * Shard.SHARD_SIZE + 20, 10 * Shard.SHARD_SIZE))) {
            AttrStoreBuilder asb = new AttrStoreBuilder(DoubleShard::create);
            strings.forEach(asb::add);
            assertThat(asb.size(), equalTo(strings.size()));
            AttrStore store = asb.build();
            assertThat(store.size(), equalTo(strings.size()));
            assertThat(IntStream.range(0, strings.size())
                                .mapToObj(store::get)
                                .collect(Collectors.toList()),
                       contains(strings.toArray()));
        }
    }

    @Test
    public void testSwap() {
        AttrStoreBuilder asb = new AttrStoreBuilder();
        asb.add("foo");
        asb.add("bar");
        asb.swap(0, 1);
        assertThat(asb.get(0), equalTo("bar"));
        assertThat(asb.get(1), equalTo("foo"));

        AttrStore as = asb.build();
        assertThat(as.get(0), equalTo("bar"));
        assertThat(as.get(1), equalTo("foo"));
    }

    @Test
    public void testSwapSkip() {
        AttrStoreBuilder asb = new AttrStoreBuilder();
        asb.add("foo");
        asb.skip();
        asb.swap(0, 1);
        assertThat(asb.get(0), nullValue());
        assertThat(asb.get(1), equalTo("foo"));

        AttrStore as = asb.build();
        assertThat(as.get(0), nullValue());
        assertThat(as.get(1), equalTo("foo"));
    }

    @Test
    public void testSwapAcrossShards() {
        AttrStoreBuilder asb = new AttrStoreBuilder();
        for (int i = 0; i < Shard.SHARD_SIZE + 50; i++) {
            asb.add(Integer.toString(i));
        }

        asb.swap(0, Shard.SHARD_SIZE + 2);
        assertThat(asb.get(0), equalTo(Integer.toString(Shard.SHARD_SIZE + 2)));
        assertThat(asb.get(Shard.SHARD_SIZE + 2), equalTo("0"));

        AttrStore as = asb.build();
        assertThat(as.get(0), equalTo(Integer.toString(Shard.SHARD_SIZE + 2)));
        assertThat(as.get(Shard.SHARD_SIZE + 2), equalTo("0"));
    }

    @Test
    public void testSwapAndUpgrade() {
        AttrStoreBuilder asb = new AttrStoreBuilder(LongShard::create);
        for (long i = 0; i < Shard.SHARD_SIZE; i++) {
            asb.add(i);
        }
        for (long i = 0; i < 50; i++) {
            asb.add(Integer.MAX_VALUE + i);
        }

        asb.swap(0, Shard.SHARD_SIZE + 2);
        assertThat(asb.get(0), equalTo(Integer.MAX_VALUE + 2L));
        assertThat(asb.get(Shard.SHARD_SIZE + 2), equalTo(0L));

        AttrStore as = asb.build();
        assertThat(as.get(0), equalTo(Integer.MAX_VALUE + 2L));
        assertThat(as.get(Shard.SHARD_SIZE + 2), equalTo(0L));
    }

    @Test
    public void testSwapAndUpgradeReverse() {
        AttrStoreBuilder asb = new AttrStoreBuilder(LongShard::create);
        for (long i = 0; i < Shard.SHARD_SIZE; i++) {
            asb.add(i);
        }
        for (long i = 0; i < 50; i++) {
            asb.add(Integer.MAX_VALUE + i);
        }

        asb.swap(Shard.SHARD_SIZE + 2, 0);
        assertThat(asb.get(0), equalTo(Integer.MAX_VALUE + 2L));
        assertThat(asb.get(Shard.SHARD_SIZE + 2), equalTo(0L));

        AttrStore as = asb.build();
        assertThat(as.get(0), equalTo(Integer.MAX_VALUE + 2L));
        assertThat(as.get(Shard.SHARD_SIZE + 2), equalTo(0L));
    }
}