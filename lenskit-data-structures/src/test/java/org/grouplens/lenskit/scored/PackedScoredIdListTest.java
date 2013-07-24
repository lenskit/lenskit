package org.grouplens.lenskit.scored;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
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
        assertThat(list.fastIterator().hasNext(), equalTo(false));
    }

    @Test
    public void testAddScoredId() {
        builder.add(new ScoredIdBuilder(42, 3.5).build());
        PackedScoredIdList list = builder.build();
        assertThat(list.size(), equalTo(1));
        assertThat(list.isEmpty(), equalTo(false));
        ScoredId id = list.get(0);
        assertThat(id, notNullValue());
        assertThat(id.getId(), equalTo(42L));
        assertThat(id.getScore(), equalTo(3.5));
        assertThat(id.getChannels(), hasSize(0));

        Iterator<ScoredId> iter = list.iterator();
        assertThat(Lists.newArrayList(iter),
                   contains(id));
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testAddRaw() {
        builder.add(42, 3.5);
        PackedScoredIdList list = builder.build();
        assertThat(list.size(), equalTo(1));
        assertThat(list.isEmpty(), equalTo(false));
        ScoredId id = list.get(0);
        assertThat(id, notNullValue());
        assertThat(id.getId(), equalTo(42L));
        assertThat(id.getScore(), equalTo(3.5));
        assertThat(id.getChannels(), hasSize(0));

        Iterator<ScoredId> iter = list.iterator();
        assertThat(Lists.newArrayList(iter),
                   contains(id));
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testAddWithChannels() {
        Symbol sym = Symbol.of("HACKEM MUCHE");
        builder.add(new ScoredIdBuilder(42, 3.5).addChannel(sym, Math.PI).build());
        PackedScoredIdList list = builder.build();
        assertThat(list.size(), equalTo(1));
        assertThat(list.isEmpty(), equalTo(false));
        ScoredId id = list.get(0);
        assertThat(id, notNullValue());
        assertThat(id.getId(), equalTo(42L));
        assertThat(id.getScore(), equalTo(3.5));

        assertThat(id.getChannels(), contains(sym));
        assertThat(id.channel(sym), equalTo(Math.PI));

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
        builder.add(id1);
        builder.add(id2);
        PackedScoredIdList list = builder.build();
        assertThat(list.get(0), equalTo(id1));
        assertThat(list.get(1), equalTo(id2));

        Iterator<ScoredId> iter = list.iterator();
        assertThat(Lists.newArrayList(iter),
                   contains(id1, id2));
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testAddWithDifferentChannels() {
        Symbol sym1 = Symbol.of("HACKEM MUCHE");
        Symbol sym2 = Symbol.of("READ ME");
        ScoredId id1 = new ScoredIdBuilder(42, 3.5).addChannel(sym1, Math.PI).build();
        ScoredId id2 = new ScoredIdBuilder(38, 2.6).addChannel(sym2, Math.E).build();
        builder.add(id1);
        builder.add(id2);
        PackedScoredIdList list = builder.build();
        assertThat(list.get(0), equalTo(id1));
        assertThat(list.get(1), equalTo(id2));

        Iterator<ScoredId> iter = list.iterator();
        assertThat(Lists.newArrayList(iter),
                   contains(id1, id2));
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testAddWithDifferentTyped() {
        TypedSymbol<String> sym1 = TypedSymbol.of(String.class, "HACKEM MUCHE");
        TypedSymbol<String> sym2 = TypedSymbol.of(String.class, "READ ME");
        ScoredId id1 = new ScoredIdBuilder(42, 3.5).addChannel(sym1, "foo").build();
        ScoredId id2 = new ScoredIdBuilder(38, 2.6).addChannel(sym2, "bar").build();
        builder.add(id1);
        builder.add(id2);
        PackedScoredIdList list = builder.build();
        assertThat(list.get(0), equalTo(id1));
        assertThat(list.get(1), equalTo(id2));

        Iterator<ScoredId> iter = list.iterator();
        assertThat(Lists.newArrayList(iter),
                   contains(id1, id2));
        assertThat(iter.hasNext(), equalTo(false));
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
        PackedScoredIdList list = builder.build();
        assertThat(list, hasSize(25));
        for (int i = 0; i < 25; i++) {
            ScoredId id = new ScoredIdBuilder(i, vals.get(i)).build();
            assertThat(list.get(i), equalTo(id));
        }
    }

    @Test
    public void testAddManyWithChannels() {
        Symbol sym = Symbol.of("VALUE");
        TypedSymbol<String> str = TypedSymbol.of(String.class, "STRING");
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
    public void testSort() {
        Symbol sym = Symbol.of("VALUE");
        TypedSymbol<String> str = TypedSymbol.of(String.class, "STRING");
        Random rng = new Random();
        List<ScoredId> ids = Lists.newArrayListWithCapacity(25);
        for (int i = 0; i < 25; i++) {
            double v = rng.nextGaussian() + Math.PI;
            ScoredIdBuilder bld = new ScoredIdBuilder(i, v);
            if (rng.nextBoolean()) {
                bld.addChannel(sym, Math.log(v));
            }
            if (rng.nextBoolean()) {
                bld.addChannel(str, Double.toString(v));
            }
            ScoredId id = bld.build();
            ids.add(id);
            builder.add(id);
        }
        PackedScoredIdList list = builder.sort(ScoredIds.scoreOrder().reverse()).build();
        Collections.sort(ids, ScoredIds.scoreOrder().reverse());
        assertThat(list, hasSize(25));
        for (int i = 0; i < 25; i++) {
            assertThat(list.get(i), equalTo(ids.get(i)));
        }
        // check equality for good measure
        assertThat(list, equalTo(ids));
    }

    @Test
    public void testFinish() {
        builder = new ScoredIdListBuilder(25);
        Symbol sym = Symbol.of("VALUE");
        TypedSymbol<String> str = TypedSymbol.of(String.class, "STRING");
        Random rng = new Random();
        List<ScoredId> ids = Lists.newArrayListWithCapacity(25);
        for (int i = 0; i < 25; i++) {
            double v = rng.nextGaussian() + Math.PI;
            ScoredIdBuilder bld = new ScoredIdBuilder(i, v);
            if (rng.nextBoolean()) {
                bld.addChannel(sym, Math.log(v));
            }
            if (rng.nextBoolean()) {
                bld.addChannel(str, Double.toString(v));
            }
            ScoredId id = bld.build();
            ids.add(id);
            builder.add(id);
        }
        PackedScoredIdList list = builder.sort(ScoredIds.scoreOrder().reverse()).finish();
        Collections.sort(ids, ScoredIds.scoreOrder().reverse());
        assertThat(list, hasSize(25));
        for (int i = 0; i < 25; i++) {
            assertThat(list.get(i), equalTo(ids.get(i)));
        }
        // check equality for good measure
        assertThat(list, equalTo(ids));
    }
}
