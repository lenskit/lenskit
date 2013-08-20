package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.longs.LongSortedSets;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.grouplens.lenskit.collections.LongUtils.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestLongUtils {
    @Test
    public void testUnionSizeEmptySS() {
        assertThat(unionSize(LongSortedSets.EMPTY_SET, LongSortedSets.EMPTY_SET),
                   equalTo(0));
    }

    @Test
    public void testUnionSizeEmptyLSAS() {
        LongKeyDomain kd = LongKeyDomain.empty();
        assertThat(unionSize(kd.activeSetView(), kd.clone().activeSetView()),
                   equalTo(0));
    }

    @Test
    public void testUnionSizeSame() {
        LongSortedSet set = packedSet(5, 3, 27);
        assertThat(unionSize(set, set), equalTo(3));
    }

    @Test
    public void testUnionSizeCompatLSAS() {
        LongKeyDomain lkd = LongKeyDomain.create(5, 3, 27);
        assertThat(unionSize(lkd.activeSetView(), lkd.clone().activeSetView()), equalTo(3));
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
    public void testUnionSizeCommonCompat() {
        LongKeyDomain lkd = LongKeyDomain.create(1, 2, 3, 4, 5);
        LongKeyDomain lkd2 = lkd.clone();
        // set1 will have 1, 3, 5
        lkd.setActive(1, false);
        lkd.setActive(3, false);
        // set2 will have 2, 3, 4
        lkd.setActive(0, false);
        lkd.setActive(4, false);
        LongSortedSet s1 = lkd.activeSetView();
        LongSortedSet s2 = lkd2.activeSetView();
        assertThat(unionSize(s1, s2), equalTo(5));
    }

    @Test
    public void testSetUnionEmptySS() {
        assertThat(setUnion(LongSortedSets.EMPTY_SET, LongSortedSets.EMPTY_SET),
                   hasSize(0));
    }

    @Test
    public void testSetUnionEmptyLSAS() {
        LongKeyDomain kd = LongKeyDomain.empty();
        assertThat(setUnion(kd.activeSetView(), kd.clone().activeSetView()),
                   hasSize(0));
    }

    @Test
    public void testSetUnionSame() {
        LongSortedSet set = packedSet(5, 3, 27);
        assertThat(setUnion(set, set), contains(3L, 5L, 27L));
    }

    @Test
    public void testSetUnionCompatLSAS() {
        LongKeyDomain lkd = LongKeyDomain.create(5, 3, 27);
        assertThat(setUnion(lkd.activeSetView(), lkd.clone().activeSetView()),
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

    @Test
    public void testSetUnionCommonCompat() {
        LongKeyDomain lkd = LongKeyDomain.create(1, 2, 3, 4, 5);
        LongKeyDomain lkd2 = lkd.clone();
        // set1 will have 1, 3, 5
        lkd.setActive(1, false);
        lkd.setActive(3, false);
        // set2 will have 2, 3, 4
        lkd.setActive(0, false);
        lkd.setActive(4, false);
        LongSortedSet s1 = lkd.activeSetView();
        LongSortedSet s2 = lkd2.activeSetView();
        assertThat(setUnion(s1, s2),
                   contains(1L, 2L, 3L, 4L, 5L));
    }

    @Test
    public void testSetUnionIncludeMaxLong() {
        LongSortedSet s1 = packedSet(1, 2, 3, Long.MAX_VALUE);
        LongSortedSet s2 = packedSet(4, 5, 6);
        assertThat(setUnion(s1, s2),
                   contains(1L, 2L, 3L, 4L, 5L, 6L, Long.MAX_VALUE));
    }
}
