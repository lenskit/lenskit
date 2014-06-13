/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.longs.LongList;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CompactableLongArrayListTest {
    @Test
    public void testEmptyList() {
        LongList list = new CompactableLongArrayList();
        assertThat(list.size(), equalTo(0));
        assertThat(list.isEmpty(), equalTo(true));
    }

    @Test
    public void testSingleton() {
        LongList list = new CompactableLongArrayList();
        list.add(42L);
        assertThat(list.size(), equalTo(1));
        assertThat(list.isEmpty(), equalTo(false));
        assertThat(list, contains(42L));
    }

    @Test
    public void testSingletonLong() {
        LongList list = new CompactableLongArrayList();
        long val = Integer.MAX_VALUE + 42L;
        list.add(val);
        assertThat(list.size(), equalTo(1));
        assertThat(list.isEmpty(), equalTo(false));
        assertThat(list, contains(val));
    }

    @Test
    public void testSingletonNegativeLong() {
        LongList list = new CompactableLongArrayList();
        long val = Integer.MIN_VALUE - 42L;
        list.add(val);
        assertThat(list.size(), equalTo(1));
        assertThat(list.isEmpty(), equalTo(false));
        assertThat(list, contains(val));
    }

    @Test
    public void testAddTwo() {
        LongList list = new CompactableLongArrayList();
        long val = Integer.MAX_VALUE + 42L;
        list.add(42);
        list.add(val);
        assertThat(list.size(), equalTo(2));
        assertThat(list, contains(42L, val));
    }

    @Test
    public void testAddAndPrepend() {
        LongList list = new CompactableLongArrayList();
        long val = 67L;
        list.add(42);
        list.add(0, val);
        assertThat(list.size(), equalTo(2));
        assertThat(list, contains(val, 42L));
    }

    @Test
    public void testAddAndPrependUpgrade() {
        LongList list = new CompactableLongArrayList();
        long val = Integer.MAX_VALUE + 42L;
        list.add(42);
        list.add(0, val);
        assertThat(list.size(), equalTo(2));
        assertThat(list, contains(val, 42L));
        assertThat(list.get(0), equalTo(val));
        assertThat(list.get(1), equalTo(42L));
    }

    @Test
    public void testSetReplace() {
        LongList list = new CompactableLongArrayList();
        long val = 67L;
        list.add(42);
        list.add(37);
        list.add(4);
        assertThat(list.set(1, val), equalTo(37L));
        assertThat(list.size(), equalTo(3));
        assertThat(list, contains(42L, val, 4L));
    }

    @Test
    public void testSetUpgrade() {
        LongList list = new CompactableLongArrayList();
        long val = Integer.MAX_VALUE + 42L;
        list.add(42);
        list.add(37);
        list.add(4);
        assertThat(list.set(1, val), equalTo(37L));
        assertThat(list.size(), equalTo(3));
        assertThat(list, contains(42L, val, 4L));
    }

    @Test
    public void testSerializeCompact() {
        CompactableLongArrayList list = new CompactableLongArrayList();
        list.add(42);
        list.add(37);
        list.add(4);

        LongList copy = SerializationUtils.clone(list);
        assertThat(copy, contains(42L, 37L, 4L));
    }

    @Test
    public void testSerializeFull() {
        CompactableLongArrayList list = new CompactableLongArrayList();
        list.add(42);
        list.add(37);
        list.add(Integer.MAX_VALUE + 7L);

        LongList copy = SerializationUtils.clone(list);
        assertThat(copy, contains(42L, 37L, Integer.MAX_VALUE + 7L));
    }

    @Test
    public void testTrimCompact() {
        CompactableLongArrayList list = new CompactableLongArrayList();
        list.add(42);
        list.add(Integer.MAX_VALUE + 37L);
        list.add(7L);
        list.set(1, 37L);
        list.trim();
        assertThat(list, hasSize(3));
        assertThat(list, contains(42L, 37L, 7L));
    }
}
