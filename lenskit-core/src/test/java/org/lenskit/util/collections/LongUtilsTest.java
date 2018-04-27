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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.longs.*;
import org.junit.Test;
import org.lenskit.util.keys.SortedKeyIndex;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.lenskit.util.collections.LongUtils.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LongUtilsTest {
    @Test
    public void testEmptyRanks() {
        assertThat(itemRanks(LongLists.EMPTY_LIST).size(),
                   equalTo(0));
    }

    @Test
    public void testSingletonRanks() {
        Long2IntMap ranks = itemRanks(LongLists.singleton(1));
        assertThat(ranks.keySet(), contains(1L));
        assertThat(ranks, hasEntry(1L, 0));
    }

    @Test
    public void testTwoItemRanks() {
        Long2IntMap ranks = itemRanks(LongArrayList.wrap(new long[]{1,2}));
        assertThat(ranks.keySet(), containsInAnyOrder(1L, 2L));
        assertThat(ranks, hasEntry(1L, 0));
        assertThat(ranks, hasEntry(2L, 1));
    }

    @Test
    public void testThreeItemRanks() {
        Long2IntMap ranks = itemRanks(LongArrayList.wrap(new long[]{1,2,3}));
        assertThat(ranks.keySet(), containsInAnyOrder(1L, 2L, 3L));
        assertThat(ranks, hasEntry(1L, 0));
        assertThat(ranks, hasEntry(2L, 1));
        assertThat(ranks, hasEntry(3L, 2));
    }

    @Test
    public void testUnionSizeEmptySS() {
        assertThat(unionSize(LongSortedSets.EMPTY_SET, LongSortedSets.EMPTY_SET),
                   equalTo(0));
    }

    @Test
    public void testUnionSizeEmptyLSAS() {
        SortedKeyIndex kd = SortedKeyIndex.empty();
        assertThat(unionSize(kd.keySet(), kd.keySet()),
                   equalTo(0));
    }

    @Test
    public void testUnionSizeSame() {
        LongSortedSet set = packedSet(5, 3, 27);
        assertThat(unionSize(set, set), equalTo(3));
    }

    @Test
    public void testUnionSizeCompatLSAS() {
        SortedKeyIndex lkd = SortedKeyIndex.create(5, 3, 27);
        assertThat(unionSize(lkd.keySet(), lkd.keySet()), equalTo(3));
    }

    @Test
    public void testUnionSizeDistinctEqual() {
        LongSortedSet s1 = packedSet(5, 3, 27);
        LongSortedSet s2 = new LongAVLTreeSet(s1);
        assertThat(unionSize(s1, s2), equalTo(3));
    }

    @Test
    public void testUnionSizeNoOverlap() {
        LongSortedSet s1 = packedSet(1, 2, 3);
        LongSortedSet s2 = packedSet(4, 5, 6);
        assertThat(unionSize(s1, s2), equalTo(6));
    }

    @Test
    public void testUnionSizeInterleaved() {
        LongSortedSet s1 = packedSet(1, 3, 5);
        LongSortedSet s2 = packedSet(2, 4, 6);
        assertThat(unionSize(s1, s2), equalTo(6));
    }

    @Test
    public void testUnionSizeCommon() {
        LongSortedSet s1 = packedSet(1, 3, 5);
        LongSortedSet s2 = packedSet(2, 3, 4);
        assertThat(unionSize(s1, s2), equalTo(5));
    }

    @Test
    public void testSetUnionEmptySS() {
        assertThat(setUnion(LongSortedSets.EMPTY_SET, LongSortedSets.EMPTY_SET),
                   hasSize(0));
    }

    @Test
    public void testSetUnionEmptyLSAS() {
        SortedKeyIndex kd = SortedKeyIndex.empty();
        assertThat(setUnion(kd.keySet(), kd.keySet()),
                   hasSize(0));
    }

    @Test
    public void testSetUnionSame() {
        LongSortedSet set = packedSet(5, 3, 27);
        assertThat(setUnion(set, set), contains(3L, 5L, 27L));
    }

    @Test
    public void testSetUnionCompatLSAS() {
        SortedKeyIndex lkd = SortedKeyIndex.create(5, 3, 27);
        assertThat(setUnion(lkd.keySet(), lkd.keySet()),
                   contains(3L, 5L, 27L));
    }

    @Test
    public void testSetUnionDistinctEqual() {
        LongSortedSet s1 = packedSet(5, 3, 27);
        LongSortedSet s2 = new LongAVLTreeSet(s1);
        assertThat(setUnion(s1, s2),
                   contains(3L, 5L, 27L));
    }

    @Test
    public void testSetUnionNoOverlap() {
        LongSortedSet s1 = packedSet(1, 2, 3);
        LongSortedSet s2 = packedSet(4, 5, 6);
        assertThat(setUnion(s1, s2),
                   contains(1L, 2L, 3L, 4L, 5L, 6L));
    }

    @Test
    public void testSetUnionInterleaved() {
        LongSortedSet s1 = packedSet(1, 3, 5);
        LongSortedSet s2 = packedSet(2, 4, 6);
        assertThat(setUnion(s1, s2),
                   contains(1L, 2L, 3L, 4L, 5L, 6L));
    }

    @Test
    public void testSetUnionCommon() {
        LongSortedSet s1 = packedSet(1, 3, 5);
        LongSortedSet s2 = packedSet(2, 3, 4);
        assertThat(setUnion(s1, s2),
                   contains(1L, 2L, 3L, 4L, 5L));
    }

    @Test(timeout=500)
    public void testSetUnionIncludeMaxLong() {
        LongSortedSet s1 = packedSet(1, 2, 3, Long.MAX_VALUE);
        LongSortedSet s2 = packedSet(4, 5, 6);
        assertThat(setUnion(s1, s2),
                   contains(1L, 2L, 3L, 4L, 5L, 6L, Long.MAX_VALUE));
    }

    @Test(timeout=500)
    public void testSetUnionIncludeMaxLongSetB() {
        LongSortedSet s1 = packedSet(1, 2, 3);
        LongSortedSet s2 = packedSet(4, 5, 6, Long.MAX_VALUE);
        assertThat(setUnion(s1, s2),
                   contains(1L, 2L, 3L, 4L, 5L, 6L, Long.MAX_VALUE));
    }

    @Test
    public void testIntersectSizeEmpty() {
        assertThat(intersectSize(LongSortedSets.EMPTY_SET, LongSortedSets.EMPTY_SET),
                   equalTo(0));
    }

    @Test
    public void testIntersectSizeSingleton() {
        assertThat(intersectSize(LongSortedSets.singleton(52),
                                 LongSortedSets.singleton(52)),
                   equalTo(1));
    }

    @Test
    public void testIntersectSizeDisjoint() {
        assertThat(intersectSize(LongSortedSets.singleton(52),
                                 LongSortedSets.singleton(30)),
                   equalTo(0));
        assertThat(intersectSize(LongSortedSets.singleton(30),
                                 LongSortedSets.singleton(52)),
                   equalTo(0));
    }

    @Test
    public void testIntersectSizePackedSets() {
        assertThat(intersectSize(packedSet(1L, 3L, 5L, 7L),
                                 packedSet(2L, 3L, 4L, 5L, 6L)),
                   equalTo(2));
    }

    @Test
    public void testHasNCommonSingleton() {
        assertThat(hasNCommonItems(LongSortedSets.singleton(52),
                                   LongSortedSets.singleton(52),
                                   1),
                   equalTo(true));
        assertThat(hasNCommonItems(LongSortedSets.singleton(52),
                                   LongSortedSets.singleton(42),
                                   1),
                   equalTo(false));
        assertThat(hasNCommonItems(LongSortedSets.singleton(42),
                                   LongSortedSets.singleton(52),
                                   1),
                   equalTo(false));
        assertThat(hasNCommonItems(LongSortedSets.singleton(52),
                                   LongSortedSets.singleton(52),
                                   2),
                   equalTo(false));
    }

    @Test
    public void testHasNCommonPackedSets() {
        assertThat(hasNCommonItems(packedSet(1L, 3L, 5L, 7L),
                                   packedSet(2L, 3L, 4L, 5L, 6L),
                                   1),
                   equalTo(true));
        assertThat(hasNCommonItems(packedSet(1L, 3L, 5L, 7L),
                                   packedSet(2L, 3L, 4L, 5L, 6L),
                                   2),
                   equalTo(true));
        assertThat(hasNCommonItems(packedSet(1L, 3L, 5L, 7L),
                                   packedSet(2L, 3L, 4L, 5L, 6L),
                                   3),
                   equalTo(false));
    }

    @Test
    public void testFrozenMap() {
        assertThat(frozenMap(Collections.<Long, Double>emptyMap()).size(), equalTo(0));
        assertThat(frozenMap(Collections.singletonMap(42L, 3.9)),
                   hasEntry(42L, 3.9));
    }
}
