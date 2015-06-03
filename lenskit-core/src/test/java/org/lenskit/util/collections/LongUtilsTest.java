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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.longs.LongSortedSets;
import org.junit.Test;
import org.lenskit.util.keys.LongKeyIndex;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.lenskit.util.collections.LongUtils.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LongUtilsTest {
    @Test
    public void testUnionSizeEmptySS() {
        assertThat(unionSize(LongSortedSets.EMPTY_SET, LongSortedSets.EMPTY_SET),
                   equalTo(0));
    }

    @Test
    public void testUnionSizeEmptyLSAS() {
        LongKeyIndex kd = LongKeyIndex.empty();
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
        LongKeyIndex lkd = LongKeyIndex.create(5, 3, 27);
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
        LongKeyIndex kd = LongKeyIndex.empty();
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
        LongKeyIndex lkd = LongKeyIndex.create(5, 3, 27);
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
}
