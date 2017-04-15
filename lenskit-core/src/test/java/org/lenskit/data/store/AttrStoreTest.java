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
package org.lenskit.data.store;

import org.junit.Test;
import org.omg.CORBA.LongLongSeqHelper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.java.quickcheck.generator.CombinedGenerators.nullsAnd;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static net.java.quickcheck.generator.CombinedGeneratorsIterables.*;
import static net.java.quickcheck.generator.Generators.*;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;

public class AttrStoreTest {
    @Test
    public void testEmpty() {
        AttrStoreBuilder asb = new AttrStoreBuilder();
        assertThat(asb.size(), equalTo(0));
        try {
            asb.get(0);
            fail("getting from empty builder should fail");
        } catch (Throwable th) {
            assertThat(th, instanceOf(IndexOutOfBoundsException.class));
        }

        AttrStore store = asb.build();
        assertThat(store.size(), equalTo(0));
        try {
            store.get(0);
            fail("getting from empty store should fail");
        } catch (Throwable th) {
            assertThat(th, instanceOf(IndexOutOfBoundsException.class));
        }
    }

    @Test
    public void testAddObject() {
        AttrStoreBuilder asb = new AttrStoreBuilder();
        asb.add("heffalump");
        assertThat(asb.size(), equalTo(1));
        assertThat(asb.get(0), equalTo("heffalump"));
        try {
            asb.get(1);
            fail("getting bad value from store should fail");
        } catch (Throwable th) {
            assertThat(th, instanceOf(IndexOutOfBoundsException.class));
        }

        AttrStore store = asb.build();
        assertThat(store.size(), equalTo(1));
        assertThat(store.get(0), equalTo("heffalump"));
        try {
            store.get(1);
            fail("getting bad value from store should fail");
        } catch (Throwable th) {
            assertThat(th, instanceOf(IndexOutOfBoundsException.class));
        }
    }

    @Test
    public void testAddTwoObject() {
        AttrStoreBuilder asb = new AttrStoreBuilder();
        asb.add("heffalump");
        asb.add("woozle");
        assertThat(asb.size(), equalTo(2));
        assertThat(asb.get(0), equalTo("heffalump"));
        assertThat(asb.get(1), equalTo("woozle"));
        try {
            asb.get(2);
            fail("getting bad value from store should fail");
        } catch (Throwable th) {
            assertThat(th, instanceOf(IndexOutOfBoundsException.class));
        }

        AttrStore store = asb.build();
        assertThat(store.size(), equalTo(2));
        assertThat(store.get(0), equalTo("heffalump"));
        assertThat(store.get(1), equalTo("woozle"));
        try {
            store.get(2);
            fail("getting bad value from store should fail");
        } catch (Throwable th) {
            assertThat(th, instanceOf(IndexOutOfBoundsException.class));
        }
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
        try {
            asb.get(3);
            fail("getting bad value from store should fail");
        } catch (Throwable th) {
            assertThat(th, instanceOf(IndexOutOfBoundsException.class));
        }

        AttrStore store = asb.build();
        assertThat(store.size(), equalTo(3));
        assertThat(store.get(0), equalTo("heffalump"));
        assertThat(store.get(1), nullValue());
        assertThat(store.get(2), equalTo("woozle"));
        try {
            store.get(3);
            fail("getting bad value from store should fail");
        } catch (Throwable th) {
            assertThat(th, instanceOf(IndexOutOfBoundsException.class));
        }
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
}