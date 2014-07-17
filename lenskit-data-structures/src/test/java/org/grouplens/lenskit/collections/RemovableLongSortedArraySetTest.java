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
package org.grouplens.lenskit.collections;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test long sorted array set.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RemovableLongSortedArraySetTest {
    @Test
    public void testEmptySet() {
        LongSet ls = LongKeyDomain.empty().modifiableActiveSetView();
        assertThat(ls.remove(42), equalTo(false));
    }

    @Test
    public void testRemovePresent() {
        LongKeyDomain lks = LongKeyDomain.create(42);
        LongSet ls = lks.modifiableActiveSetView();
        assertThat(ls.remove(42), equalTo(true));
        assertThat(ls.size(), equalTo(0));
        assertThat(lks.size(), equalTo(0));
        assertThat(lks.keyIsActive(42), equalTo(false));
    }

    @Test
    public void testRemoveNotPResent() {
        LongKeyDomain lks = LongKeyDomain.create(42);
        LongSet ls = lks.modifiableActiveSetView();
        assertThat(ls.remove(39), equalTo(false));
        assertThat(ls.size(), equalTo(1));
        assertThat(lks.size(), equalTo(1));
        assertThat(lks.keyIsActive(42), equalTo(true));
    }

    @Test
    public void testRemoveAll() {
        LongKeyDomain lks = LongKeyDomain.create(20, 25, 30, 42, 62);
        LongSet ls = lks.modifiableActiveSetView();
        List<Long> rm = Longs.asList(20, 25, 62, 30, 98, 1);
        assertThat(ls.removeAll(rm), equalTo(true));
        assertThat(ls, contains(42L));
        assertThat(lks.size(), equalTo(1));
    }

    @Test
    public void testRetainAll() {
        LongKeyDomain lks = LongKeyDomain.create(20, 25, 30, 42, 62, 99);
        LongSet ls = lks.modifiableActiveSetView();
        List<Long> rm = Longs.asList(20, 25, 62, 30, 98, 1);
        assertThat(ls.retainAll(rm), equalTo(true));
        assertThat(ls, contains(20L, 25L, 30L, 62L));
        assertThat(Lists.newArrayList(lks.activeIndexIterator(false)),
                   contains(0, 1, 2, 4));
    }

    @Test
    public void testRetainAllEmptyList() {
        LongKeyDomain lks = LongKeyDomain.create(20, 25, 30, 42, 62, 99);
        LongSet ls = lks.modifiableActiveSetView();
        assertThat(ls.retainAll(Lists.newArrayList()), equalTo(true));
        assertThat(ls.isEmpty(), equalTo(true));
    }

    @Test
    public void testRetainAllLongMaxLong() {
        LongKeyDomain lks = LongKeyDomain.create(20, Long.MAX_VALUE);
        LongSet ls = lks.modifiableActiveSetView();
        assertThat(ls.retainAll(Lists.newArrayList()), equalTo(true));
        assertThat(ls.isEmpty(), equalTo(true));
    }
}
