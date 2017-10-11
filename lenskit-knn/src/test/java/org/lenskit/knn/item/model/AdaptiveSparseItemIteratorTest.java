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
package org.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.*;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.SortedKeyIndex;

import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AdaptiveSparseItemIteratorTest {
    ItemItemBuildContext context;
    @Before
    public void createContext() {
        LongSortedSet universe = LongUtils.packedSet(1, 2, 5, 7, 9, 13, 14, 17, 68, 32, 97);
        Long2ObjectMap<LongSortedSet> userItems = new Long2ObjectOpenHashMap<>();
        userItems.put(42, LongUtils.packedSet(2, 5, 9));
        userItems.put(39, LongUtils.packedSet(2, 7, 9, 13));
        userItems.put(12, universe.subSet(2, 97));
        context = new ItemItemBuildContext(SortedKeyIndex.fromCollection(universe),
                                           new Long2DoubleSortedMap[universe.size()], userItems);
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
