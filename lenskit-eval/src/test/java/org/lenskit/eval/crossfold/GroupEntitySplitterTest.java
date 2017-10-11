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
package org.lenskit.eval.crossfold;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.junit.Test;
import org.lenskit.util.collections.LongUtils;

import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class GroupEntitySplitterTest {
    @Test
    public void testPartitionN() {
        LongSet items = LongUtils.packedSet(1, 2, 3, 4, 5);
        GroupEntitySplitter split = GroupEntitySplitter.partition();
        Long2IntMap splits = split.splitEntities(items, 5, new Random());
        assertThat(splits.keySet(), equalTo(items));
        assertThat(splits.values(), containsInAnyOrder(0, 1, 2, 3, 4));
    }

    @Test
    public void testPartitionABunch() {
        LongSet items = new LongOpenHashSet();
        for (int i = 0; i < 25; i++) {
            items.add(i + 1);
        }

        GroupEntitySplitter split = GroupEntitySplitter.partition();
        Long2IntMap splits = split.splitEntities(items, 5, new Random());
        assertThat(splits.keySet(), equalTo(items));
        assertThat(new IntOpenHashSet(splits.values()),
                   containsInAnyOrder(0, 1, 2, 3, 4));
        int[] counts = new int[5];
        for (int sp: splits.values()) {
            counts[sp] += 1;
        }
        assertThat(new IntArrayList(counts),
                   everyItem(equalTo(5)));
    }

    @Test
    public void testSampleN() {
        LongSet items = LongUtils.packedSet(1, 2, 3, 4, 5);
        GroupEntitySplitter split = GroupEntitySplitter.disjointSample(1);
        Long2IntMap splits = split.splitEntities(items, 5, new Random());
        assertThat(splits.keySet(), equalTo(items));
        assertThat(splits.values(), containsInAnyOrder(0, 1, 2, 3, 4));
    }

    @Test
    public void testSampleABunch() {
        LongSet items = new LongOpenHashSet();
        for (int i = 0; i < 25; i++) {
            items.add(i + 1);
        }

        GroupEntitySplitter split = GroupEntitySplitter.disjointSample(3);
        Long2IntMap splits = split.splitEntities(items, 5, new Random());
        assertThat(splits.size(), equalTo(15));
        assertThat(splits.keySet(), everyItem(isIn(items)));
        assertThat(new IntOpenHashSet(splits.values()),
                   containsInAnyOrder(0, 1, 2, 3, 4));
        int[] counts = new int[5];
        for (int sp: splits.values()) {
            counts[sp] += 1;
        }
        assertThat(new IntArrayList(counts),
                   everyItem(equalTo(3)));
    }

    @Test
    public void testSampleButICant() {
        LongSet items = new LongOpenHashSet();
        for (int i = 0; i < 25; i++) {
            items.add(i + 1);
        }

        GroupEntitySplitter split = GroupEntitySplitter.disjointSample(10);
        Long2IntMap splits = split.splitEntities(items, 5, new Random());
        assertThat(splits.keySet(), equalTo(items));
        assertThat(new IntOpenHashSet(splits.values()),
                   containsInAnyOrder(0, 1, 2, 3, 4));
        int[] counts = new int[5];
        for (int sp: splits.values()) {
            counts[sp] += 1;
        }
        assertThat(new IntArrayList(counts),
                   everyItem(equalTo(5)));
    }
}