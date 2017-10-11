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
import org.junit.Test;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;

import static org.junit.Assert.*;

public class ItemItemBuildContextTest {

    /**
     * Test ItemItemBuildContext when all items have rating data.
     */
    @Test
    public void testAllItemsData() {
        SortedKeyIndex items = SortedKeyIndex.create(1, 2, 3, 4);

        long[] userIds = {101, 102, 103, 104};
        SortedKeyIndex idx = SortedKeyIndex.create(userIds);
        double[] ratings1 = {4.0, 3.0, 2.5, 2.0};
        double[] ratings2 = {3.0, 2.5, 4.0, 1.0};
        double[] ratings3 = {5.0, 3.5, 0.5, 1.0};
        double[] ratings4 = {4.5, 3.0, 3.5, 1.5};
        Long2DoubleSortedArrayMap v1 = Long2DoubleSortedArrayMap.wrap(idx, ratings1);
        Long2DoubleSortedArrayMap v2 = Long2DoubleSortedArrayMap.wrap(idx, ratings2);
        Long2DoubleSortedArrayMap v3 = Long2DoubleSortedArrayMap.wrap(idx, ratings3);
        Long2DoubleSortedArrayMap v4 = Long2DoubleSortedArrayMap.wrap(idx, ratings4);

        Long2DoubleSortedArrayMap[] ratings = { v1, v2, v3, v4 };
        ItemItemBuildContext context = new ItemItemBuildContext(items, ratings,
                                                                new Long2ObjectOpenHashMap<LongSortedSet>());

        testRatingIntegrity(items, ratings, context);
    }

    /**
     * Test ItemItemBuildContext when some items have rating data.
     */
    @Test
    public void testSomeItemsData() {
        SortedKeyIndex items = SortedKeyIndex.create(1, 2, 3, 4);

        long[] userIds = {101, 102, 103, 104};
        SortedKeyIndex idx = SortedKeyIndex.create(userIds);
        double[] ratings1 = {4.0, 3.0, 2.5, 2.0};
        double[] ratings4 = {4.5, 3.0, 3.5, 1.5};
        Long2DoubleSortedArrayMap v1 = Long2DoubleSortedArrayMap.wrap(idx, ratings1);
        Long2DoubleSortedArrayMap v4 = Long2DoubleSortedArrayMap.wrap(idx, ratings4);

        Long2DoubleSortedMap[] ratingMap = {
                v1,
                Long2DoubleSortedMaps.EMPTY_MAP,
                Long2DoubleSortedMaps.EMPTY_MAP,
                Long2DoubleSortedMaps.EMPTY_MAP,
                v4
        };
        ItemItemBuildContext context = new ItemItemBuildContext(items, ratingMap,
                                                                new Long2ObjectOpenHashMap<LongSortedSet>());

        testRatingIntegrity(items, ratingMap, context);
    }

    /**
     * Test ItemItemBuildContext when no items have rating data.
     */
    @Test
    public void testNoItemsData() {
        SortedKeyIndex items = SortedKeyIndex.create(1, 2, 3, 4);

        Long2DoubleSortedMap[] ratingMap = {
                Long2DoubleSortedMaps.EMPTY_MAP,
                Long2DoubleSortedMaps.EMPTY_MAP,
                Long2DoubleSortedMaps.EMPTY_MAP,
                Long2DoubleSortedMaps.EMPTY_MAP
        };
        ItemItemBuildContext context = new ItemItemBuildContext(items, ratingMap,
                                                                new Long2ObjectOpenHashMap<LongSortedSet>());

        testRatingIntegrity(items, ratingMap, context);
    }

    /**
     * Test ItemItemBuildContext when there is no rating data.
     */
    @Test
    public void testEmpty() {
        SortedKeyIndex items = SortedKeyIndex.create();
        Long2DoubleSortedMap[] ratingMap = new Long2DoubleSortedMap[0];
        ItemItemBuildContext context = new ItemItemBuildContext(items, ratingMap,
                                                                new Long2ObjectOpenHashMap<LongSortedSet>());

        testRatingIntegrity(items, ratingMap, context);
    }

    @SuppressWarnings("deprecation")
    private void testRatingIntegrity(SortedKeyIndex items, Long2DoubleMap[] trueRatings, ItemItemBuildContext context) {
        for (long itemId : context.getItems()) {
            assertEquals(trueRatings[items.tryGetIndex(itemId)], context.itemVector(itemId));
        }

    }
}
