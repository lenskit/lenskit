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
package org.lenskit.eval.traintest.recommend;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.collections.LongUtils;
import org.junit.Test;
import org.lenskit.eval.traintest.TestUser;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ItemSelectorTest {
    @Test
    public void testUniverse() {
        LongSet items = LongUtils.packedSet(42, 37, 39, 102);
        ItemSelector selector = ItemSelector.compileSelector("allItems");
        LongSet selected = selector.selectItems(items, TestUser.newBuilder().setUserId(42).build());
        assertThat(selected, equalTo(items));
    }

    @Test
    public void testTestItems() {
        LongSet items = LongUtils.packedSet(42, 37, 39, 102);
        TestUser user = TestUser.newBuilder()
                                .setUserId(42)
                                .addTestRating(1, 3.5)
                                .addTestRating(2, 4.2)
                                .build();
        ItemSelector selector = ItemSelector.compileSelector("user.testItems");
        LongSet selected = selector.selectItems(items, user);
        assertThat(selected, containsInAnyOrder(1L, 2L));
    }

    @Test
    public void testExpression() {
        LongSet items = LongUtils.packedSet(42, 37, 39, 102);
        TestUser user = TestUser.newBuilder()
                                .setUserId(42)
                                .addTestRating(1, 3.5)
                                .addTestRating(39, 4.2)
                                .build();
        ItemSelector selector = ItemSelector.compileSelector("allItems - user.testItems");
        LongSet selected = selector.selectItems(items, user);
        assertThat(selected, containsInAnyOrder(42L, 37L, 102L));
    }

    @Test
    public void testRandom() {
        LongSet items = LongUtils.packedSet(42, 37, 39, 102);
        TestUser user = TestUser.newBuilder()
                                .setUserId(42)
                                .addTestRating(1, 3.5)
                                .addTestRating(39, 4.2)
                                .build();
        ItemSelector selector = ItemSelector.compileSelector("user.testItems + pickRandom(allItems - user.testItems, 2)");
        LongSet selected = selector.selectItems(items, user);
        assertThat(selected, allOf(hasItem(1L), hasItem(39L)));
        assertThat(selected, hasSize(4));
    }
}
