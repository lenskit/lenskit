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

import it.unimi.dsi.fastutil.longs.LongList;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;
import org.lenskit.util.collections.CompactableLongArrayList;

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
