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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.SerializationUtils;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * More tests for scored IDs, on several random tests.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@RunWith(Parameterized.class)
public class RandomScoredIdListTest {
    private static Symbol VAL_SYM = Symbol.of("VALUE");
    private static TypedSymbol<String> STR_SYM = TypedSymbol.of(String.class, "STRING");

    // generate 10 random lists of ids to test with
    private static final int TEST_COUNT = 10;
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Random rng = new Random();
        List<Object[]> idLists = Lists.newArrayListWithCapacity(TEST_COUNT);
        for (int test = 0; test < TEST_COUNT; test++) {
            int size = rng.nextInt(50);
            ImmutableList.Builder<ScoredId> ids = ImmutableList.builder();
            for (int i = 0; i < size; i++) {
                double v = rng.nextGaussian() + Math.PI;
                ScoredIdBuilder bld = new ScoredIdBuilder(i, v);
                if (rng.nextBoolean()) {
                    bld.addChannel(VAL_SYM, Math.log(v));
                }
                if (rng.nextBoolean()) {
                    bld.addChannel(STR_SYM, Double.toString(v));
                }
                ScoredId id = bld.build();
                ids.add(id);
            }
            idLists.add(new Object[]{ids.build()});
        }
        return idLists;
    }

    private final List<ScoredId> idList;
    private final List<ScoredId> idsWithDefaults;
    private final int size;
    private ScoredIdListBuilder builder;

    public RandomScoredIdListTest(List<ScoredId> ids) {
        idList = ids;
        size = idList.size();
        // prepare a list of IDs with defaults
        ImmutableList.Builder<ScoredId> bld = ImmutableList.builder();
        for (ScoredId id: ids) {
            ScoredIdBuilder idBld = ScoredIds.copyBuilder(id);
            if (!id.hasUnboxedChannel(VAL_SYM)) {
                idBld.addChannel(VAL_SYM, 0);
            }
            bld.add(idBld.build());
        }
        idsWithDefaults = bld.build();
    }

    /**
     * Set up a builder containing all the IDs.
     */
    @Before
    public void initializeBuilder() {
        builder = ScoredIds.newListBuilder();
        builder.addChannel(VAL_SYM)
               .addChannel(STR_SYM);
        for (ScoredId id: idList) {
            builder.add(id);
        }
    }

    @Test
    public void testIterateFast() {
        PackedScoredIdList list = builder.build();
        assertThat(list, hasSize(size));
        int i = 0;
        for (ScoredId id: list) {
            assertThat(id, equalTo(idsWithDefaults.get(i)));
            i++;
        }
    }

    @Test
    public void testSort() {
        PackedScoredIdList list = builder.sort(ScoredIds.scoreOrder().reverse()).build();
        List<ScoredId> sorted = ScoredIds.scoreOrder().reverse().sortedCopy(idsWithDefaults);
        assertThat(list, hasSize(size));
        for (int i = 0; i < size; i++) {
            assertThat(list.get(i), equalTo(sorted.get(i)));
        }
        // check equality for good measure
        assertThat(list, equalTo(sorted));
    }

    @Test
    public void testIdSort() {
        PackedScoredIdList list = builder.sort(ScoredIds.idOrder().reverse()).build();
        List<ScoredId> sorted = ScoredIds.idOrder().reverse().sortedCopy(idsWithDefaults);
        assertThat(list, hasSize(size));
        for (int i = 0; i < size; i++) {
            assertThat(list.get(i), equalTo(sorted.get(i)));
        }
        // check equality for good measure
        assertThat(list, equalTo(sorted));
    }

    /**
     * Test sorting by channel, to make sure the plumbing to make channels available in IDs at
     * sort time works properly.
     */
    @Test
    public void testChannelSort() {
        PackedScoredIdList list = builder.sort(ScoredIds.channelOrder(VAL_SYM)
                                                        .compound(ScoredIds.scoreOrder())).build();
        List<ScoredId> sorted = ScoredIds.channelOrder(VAL_SYM)
                                         .compound(ScoredIds.scoreOrder())
                                         .sortedCopy(idsWithDefaults);
        assertThat(list, hasSize(size));
        for (int i = 0; i < size; i++) {
            assertThat(list.get(i), equalTo(sorted.get(i)));
        }
        // check equality for good measure
        assertThat(list, equalTo(sorted));
    }

    /**
     * Test sorting by typed channel, to make sure the plumbing to make channels available in IDs at
     * sort time works properly.
     */
    @Test
    public void testTypedChannelSort() {
        PackedScoredIdList list = builder.sort(ScoredIds.channelOrder(STR_SYM)
                                                        .nullsFirst()
                                                        .compound(ScoredIds.scoreOrder())).build();
        List<ScoredId> sorted = ScoredIds.channelOrder(STR_SYM)
                                         .nullsFirst()
                                         .compound(ScoredIds.scoreOrder())
                                         .sortedCopy(idsWithDefaults);
        assertThat(list, hasSize(size));
        for (int i = 0; i < size; i++) {
            assertThat(list.get(i), equalTo(sorted.get(i)));
        }
        // check equality for good measure
        assertThat(list, equalTo(sorted));
    }

    @Test
    public void testFinish() {
        PackedScoredIdList list = builder.finish();
        assertThat(list, hasSize(size));
        for (int i = 0; i < size; i++) {
            assertThat(list.get(i), equalTo(idsWithDefaults.get(i)));
        }
        // check equality for good measure
        assertThat(list, equalTo(idsWithDefaults));
    }

    @Test
    public void testSerialize() {
        PackedScoredIdList list = builder.finish();
        PackedScoredIdList l2 = SerializationUtils.clone(list);
        assertThat(l2, not(sameInstance(list)));
        assertThat(l2, equalTo(list));
    }
}
