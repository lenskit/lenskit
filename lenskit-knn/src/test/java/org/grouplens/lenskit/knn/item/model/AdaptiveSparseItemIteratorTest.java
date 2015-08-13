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
package org.grouplens.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.*;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.LongKeyDomain;

import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AdaptiveSparseItemIteratorTest {
    ItemItemBuildContext context;
    @Before
    public void createContext() {
        LongSortedSet universe = LongUtils.packedSet(1, 2, 5, 7, 9, 13, 14, 17, 68, 32, 97);
        Long2ObjectMap<LongSortedSet> userItems = new Long2ObjectOpenHashMap<LongSortedSet>();
        userItems.put(42, LongUtils.packedSet(2, 5, 9));
        userItems.put(39, LongUtils.packedSet(2, 7, 9, 13));
        userItems.put(12, universe.subSet(2, 97));
        context = new ItemItemBuildContext(LongKeyDomain.fromCollection(universe),
                                           null, userItems);
    }

    @Test
    public void testEmptyUserSet() {
        LongIterator iter = new AdaptiveSparseItemIterator(context, LongSets.EMPTY_SET);
        assertThat(iter.hasNext(), equalTo(false));
        try {
            iter.nextLong();
            fail("nextLong() should throw NSEE");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }

    @Test
    public void testSingleUserSet() {
        LongIterator iter = new AdaptiveSparseItemIterator(context, LongUtils.packedSet(42));
        LongList items = LongIterators.pour(iter);
        assertThat(items, containsInAnyOrder(2L, 5L, 9L));
    }

    @Test
    public void testTwoUserSet() {
        LongIterator iter = new AdaptiveSparseItemIterator(context, LongUtils.packedSet(42, 39));
        LongList items = LongIterators.pour(iter);
        assertThat(items, hasSize(5));
        assertThat(items, containsInAnyOrder(2L, 5L, 9L, 7L, 13L));
    }

    @Test
    public void testTwoUserSetInvasive() {
        LongIterator iter = new AdaptiveSparseItemIterator(context, LongUtils.packedSet(42, 39));
        // this test depends on implementation details
        // first, get all the items from the first user (39)
        LongList items = new LongArrayList();
        items.add(iter.nextLong());
        items.add(iter.nextLong());
        items.add(iter.nextLong());
        items.add(iter.nextLong());
        assertThat(items, containsInAnyOrder(2L, 7L, 9L, 13L));
        // then check that we have next
        assertThat(iter.hasNext(), equalTo(true));
        // again to make sure it isn't messed up
        assertThat(iter.hasNext(), equalTo(true));
        // see if the next item is the first unique item from the second user
        assertThat(iter.nextLong(), equalTo(5L));
        // and that's the only unique
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testLowerBound() {
        LongIterator iter = new AdaptiveSparseItemIterator(context, LongUtils.packedSet(42, 39), 5);
        LongList items = LongIterators.pour(iter);
        assertThat(items, hasSize(3));
        assertThat(items, containsInAnyOrder(9L, 7L, 13L));
    }

    @Test
    public void testAdaptive() {
        LongIterator iter = new AdaptiveSparseItemIterator(context, LongUtils.packedSet(12, 42));
        LongList items = LongIterators.pour(iter);
        // since 12 has too many items, it will use all items instead of 42's items
        assertThat(items, hasSize(context.getItems().size()));
        assertThat(LongUtils.packedSet(items), equalTo(context.getItems()));
    }
}
