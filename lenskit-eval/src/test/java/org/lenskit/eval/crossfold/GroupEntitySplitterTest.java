/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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