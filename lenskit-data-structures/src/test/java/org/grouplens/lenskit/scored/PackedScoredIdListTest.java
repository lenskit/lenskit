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
package org.grouplens.lenskit.scored;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test a packed scored ID list.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PackedScoredIdListTest {
    ScoredIdListBuilder builder;

    @Before
    public void createEmptyList() {
        builder = new ScoredIdListBuilder();
    }

    @Test
    public void testEmptyList() {
        assertThat(builder.size(), equalTo(0));
        PackedScoredIdList list = builder.build();
        assertThat(list.size(), equalTo(0));
        assertThat(list.isEmpty(), equalTo(true));
        try {
            list.get(0);
            fail("get(0) on empty list should throw exception");
        } catch (IndexOutOfBoundsException e) {
            /* expected */
        }
        assertThat(list.iterator().hasNext(), equalTo(false));
    }

    @Test
    public void testAddScoredId() {
        builder.add(new ScoredIdBuilder(42, 3.5).build());
        assertThat(builder.size(), equalTo(1));
        PackedScoredIdList list = builder.build();
        assertThat(list.size(), equalTo(1));
        assertThat(list.isEmpty(), equalTo(false));
        ScoredId id = list.get(0);
        assertThat(id, notNullValue());
        assertThat(id.getId(), equalTo(42L));
        assertThat(id.getScore(), equalTo(3.5));
        assertThat(id.getUnboxedChannelSymbols(), hasSize(0));

        Iterator<ScoredId> iter = list.iterator();
        assertThat(Lists.newArrayList(iter),
                   contains(id));
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testAddRaw() {
        builder.add(42, 3.5);
        assertThat(builder.size(), equalTo(1));
        PackedScoredIdList list = builder.build();
        assertThat(list.size(), equalTo(1));
        assertThat(list.isEmpty(), equalTo(false));
        ScoredId id = list.get(0);
        assertThat(id, notNullValue());
        assertThat(id.getId(), equalTo(42L));
        assertThat(id.getScore(), equalTo(3.5));
        assertThat(id.getUnboxedChannelSymbols(), hasSize(0));

        Iterator<ScoredId> iter = list.iterator();
        assertThat(Lists.newArrayList(iter),
                   contains(id));
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testAddWithChannels() {
        Symbol sym = Symbol.of("HACKEM MUCHE");
        builder.addChannel(sym);
        builder.add(new ScoredIdBuilder(42, 3.5).addChannel(sym, Math.PI).build());
        PackedScoredIdList list = builder.build();
        assertThat(list.size(), equalTo(1));
        assertThat(list.isEmpty(), equalTo(false));
        ScoredId id = list.get(0);
        assertThat(id, notNullValue());
        assertThat(id.getId(), equalTo(42L));
        assertThat(id.getScore(), equalTo(3.5));

        assertThat(id.getUnboxedChannelSymbols(), contains(sym));
        assertThat(id.getUnboxedChannelValue(sym), equalTo(Math.PI));

        Iterator<ScoredId> iter = list.iterator();
        assertThat(Lists.newArrayList(iter),
                   contains(id));
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testAddTwo() {
        Symbol sym = Symbol.of("HACKEM MUCHE");
        ScoredId id1 = new ScoredIdBuilder(42, 3.5).addChannel(sym, Math.PI).build();
        ScoredId id2 = new ScoredIdBuilder(38, 2.6).addChannel(sym, Math.E).build();
        builder.addChannel(sym);
        builder.add(id1);
        builder.add(id2);
        assertThat(builder.size(), equalTo(2));
        PackedScoredIdList list = builder.build();
        assertThat(list.get(0), equalTo(id1));
        assertThat(list.get(1), equalTo(id2));

        Iterator<ScoredId> iter = list.iterator();
        assertThat(Lists.newArrayList(iter),
                   contains(id1, id2));
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testAddWithDefaultChannel() {
        Symbol sym = Symbol.of("HACKEM MUCHE");
        ScoredId id = new ScoredIdBuilder(42, 3.5).build();
        ScoredId withDefault = new ScoredIdBuilder(42, 3.5).addChannel(sym, -1).build();
        PackedScoredIdList list = builder.addChannel(sym, -1)
                                         .add(id)
                                         .build();
        assertThat(list.get(0), equalTo(withDefault));
    }

    @Test
    public void testAddWithDefaultTypedChannel() {
        TypedSymbol<String> sym = TypedSymbol.of(String.class, "HACKEM MUCHE");
        ScoredId id = new ScoredIdBuilder(42, 3.5).build();
        ScoredId withDefault = new ScoredIdBuilder(42, 3.5).addChannel(sym, "foo").build();
        PackedScoredIdList list = builder.addChannel(sym, "foo")
                                         .add(id)
                                         .build();
        assertThat(list.get(0), equalTo(withDefault));
    }

    @Test
    public void testAddWithDefaultDefaultChannel() {
        Symbol sym = Symbol.of("HACKEM MUCHE");
        ScoredId id = new ScoredIdBuilder(42, 3.5).build();
        ScoredId withDefault = new ScoredIdBuilder(42, 3.5).addChannel(sym, 0).build();
        PackedScoredIdList list = builder.addChannel(sym)
                                         .add(id)
                                         .build();
        assertThat(list.get(0), equalTo(withDefault));
    }

    @Test
    public void testAddWithDefaultDefaultTypedChannel() {
        TypedSymbol<String> sym = TypedSymbol.of(String.class, "HACKEM MUCHE");
        ScoredId id = new ScoredIdBuilder(42, 3.5).build();
        ScoredId withDefault = new ScoredIdBuilder(42, 3.5).build();
        PackedScoredIdList list = builder.addChannel(sym)
                                         .add(id)
                                         .build();
        assertThat(list.get(0), equalTo(withDefault));
    }

    @Test
    public void testAddMany() {
        Random rng = new Random();
        DoubleList vals = new DoubleArrayList(25);
        for (int i = 0; i < 25; i++) {
            double v = rng.nextGaussian() + Math.PI;
            builder.add(i, v);
            vals.add(v);
        }
        assertThat(builder.size(), equalTo(25));
        PackedScoredIdList list = builder.build();
        assertThat(list, hasSize(25));
        for (int i = 0; i < 25; i++) {
            ScoredId id = new ScoredIdBuilder(i, vals.getDouble(i)).build();
            assertThat(list.get(i), equalTo(id));
        }
    }

    @Test
    public void testAddManyWithChannels() {
        Symbol sym = Symbol.of("VALUE");
        TypedSymbol<String> str = TypedSymbol.of(String.class, "STRING");
        builder.addChannel(sym)
               .addChannel(str);
        Random rng = new Random();
        List<ScoredId> ids = Lists.newArrayListWithCapacity(25);
        for (int i = 0; i < 25; i++) {
            double v = rng.nextGaussian() + Math.PI;
            ScoredId id = new ScoredIdBuilder(i, v)
                    .addChannel(sym, Math.log(v))
                    .addChannel(str, Double.toString(v))
                    .build();
            ids.add(id);
            builder.add(id);
        }
        PackedScoredIdList list = builder.build();
        assertThat(list, hasSize(25));
        for (int i = 0; i < 25; i++) {
            assertThat(list.get(i), equalTo(ids.get(i)));
        }
        // check equality for good measure
        assertThat(list, equalTo(ids));
    }

    @Test
    public void testAddAll() {
        Symbol sym = Symbol.of("VALUE");
        TypedSymbol<String> str = TypedSymbol.of(String.class, "STRING");
        builder.addChannel(sym)
               .addChannel(str);
        Random rng = new Random();
        List<ScoredId> ids = Lists.newArrayListWithCapacity(25);
        for (int i = 0; i < 25; i++) {
            double v = rng.nextGaussian() + Math.PI;
            ScoredId id = new ScoredIdBuilder(i, v)
                    .addChannel(sym, Math.log(v))
                    .addChannel(str, Double.toString(v))
                    .build();
            ids.add(id);
        }
        builder.addAll(ids);
        PackedScoredIdList list = builder.build();
        assertThat(list, hasSize(25));
        for (int i = 0; i < 25; i++) {
            assertThat(list.get(i), equalTo(ids.get(i)));
        }
        // check equality for good measure
        assertThat(list, equalTo(ids));
    }

    @Test
    public void testIterateFast() {
        Symbol sym = Symbol.of("VALUE");
        TypedSymbol<String> str = TypedSymbol.of(String.class, "STRING");
        builder.addChannel(sym)
               .addChannel(str);
        Random rng = new Random();
        List<ScoredId> ids = Lists.newArrayListWithCapacity(25);
        for (int i = 0; i < 25; i++) {
            double v = rng.nextGaussian() + Math.PI;
            ScoredId id = new ScoredIdBuilder(i, v)
                    .addChannel(sym, Math.log(v))
                    .addChannel(str, Double.toString(v))
                    .build();
            ids.add(id);
            builder.add(id);
        }
        PackedScoredIdList list = builder.build();
        assertThat(list, hasSize(25));
        int i = 0;
        for (ScoredId id: list) {
            assertThat(id, equalTo(ids.get(i)));
            i++;
        }
    }

    @Test
    public void testFailOnInvalidChannel() {
        Symbol sym = Symbol.of("symbol");
        ScoredId idWithoutSymbol = new ScoredIdBuilder(72, 8.5).addChannel(sym, 3.5).build();
        try {
            builder.add(idWithoutSymbol);
            fail("add should fail with unknown symbol");
        } catch (IllegalArgumentException e) {
            /* expected */
        }
    }

    @Test
    public void testFailOnInvalidTypedChannel() {
        TypedSymbol<String> sym = TypedSymbol.of(String.class, "symbol");
        ScoredId idWithoutSymbol = new ScoredIdBuilder(72, 8.5).addChannel(sym, "foo").build();
        try {
            builder.add(idWithoutSymbol);
            fail("add should fail with unknown symbol");
        } catch (IllegalArgumentException e) {
            /* expected */
        }
    }

    @Test
    public void testIgnoreInvalidChannel() {
        Symbol sym = Symbol.of("symbol");
        ScoredId id = new ScoredIdBuilder(72, 8.5).addChannel(sym, 3.5).build();
        PackedScoredIdList list = builder.ignoreUnknownChannels()
                                         .add(id)
                                         .build();
        assertThat(FluentIterable.from(list).first().get().hasUnboxedChannel(sym),
                   equalTo(false));
    }

    @Test
    public void testIgnoreInvalidTypedChannel() {
        TypedSymbol<String> sym = TypedSymbol.of(String.class, "symbol");
        ScoredId id = new ScoredIdBuilder(72, 8.5).addChannel(sym, "foo").build();
        PackedScoredIdList list = builder.ignoreUnknownChannels()
                                         .add(id)
                                         .build();
        assertThat(FluentIterable.from(list).first().get().hasChannel(sym),
                   equalTo(false));
    }

    @Test
    public void testOmitsNullChannels() {
        TypedSymbol<String> sym = TypedSymbol.of(String.class, "foo");
        PackedScoredIdList list = builder.addChannel(sym)
                                         .add(42, 3.9)
                                         .build();
        ScoredId sid = list.get(0);
        assertThat(sid.hasChannel(sym), equalTo(false));
        assertThat(sid.getChannels(), hasSize(0));
        assertThat(sid.getChannelSymbols(), hasSize(0));
        assertThat(sid.getChannelValue(sym), nullValue());
    }

    @Test
    public void testIncludesUnboxedChannels() {
        Symbol sym = Symbol.of("foo");
        TypedSymbol<Double> tsym = sym.withType(Double.class);
        PackedScoredIdList list = builder.addChannel(sym)
                                         .add(42, 3.9)
                                         .build();
        ScoredId sid = list.get(0);
        assertThat(sid.hasUnboxedChannel(sym), equalTo(true));
        assertThat(sid.hasChannel(tsym), equalTo(true));
        assertThat(sid.getChannels(), hasSize(1));
        assertThat(sid.getChannelSymbols(), contains((TypedSymbol) tsym));
        assertThat(sid.getChannelValue(tsym), equalTo(0.0));
        assertThat(sid.getChannels().iterator().next().getSymbol(),
                   equalTo((TypedSymbol) tsym));
    }
}
