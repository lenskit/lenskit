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
package org.lenskit.eval.traintest.recommend;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.junit.Test;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.util.collections.LongUtils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ItemSelectorTest {
    @Test
    public void testUniverse() {
        LongSet items = LongUtils.packedSet(42, 37, 39, 102);
        ItemSelector selector = ItemSelector.compileSelector("allItems");
        LongSet selected = selector.selectItems(items, null, TestUser.newBuilder().setUserId(42).build());
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
        LongSet selected = selector.selectItems(items, null, user);
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
        LongSet selected = selector.selectItems(items, null, user);
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
        LongSet selected = selector.selectItems(items, null, user);
        assertThat(selected, allOf(hasItem(1L), hasItem(39L)));
        assertThat(selected, hasSize(4));
    }
}
