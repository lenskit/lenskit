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
package org.lenskit.util.keys;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class FrozenHashKeyIndexTest {
    @Test
    public void testEmpty() {
        KeyIndex empty = new FrozenHashKeyIndex(LongLists.EMPTY_LIST);
        assertThat(empty.getKeyList(), hasSize(0));
        assertThat(empty.containsKey(30), equalTo(false));
        assertThat(empty.tryGetIndex(30), equalTo(-1));
        try {
            empty.getIndex(30);
            fail("getting absent index should fail");
        } catch (IllegalArgumentException ex) {
            /* expected */
        }
    }

    @Test
    public void testSingleton() {
        KeyIndex idx = new FrozenHashKeyIndex(LongLists.singleton(42));
        assertThat(idx.getKeyList(), hasSize(1));
        assertThat(idx.getKeyList(), contains(42L));
        assertThat(idx.containsKey(30), equalTo(false));
        assertThat(idx.containsKey(42), equalTo(true));
        assertThat(idx.tryGetIndex(30), equalTo(-1));
        assertThat(idx.tryGetIndex(42), equalTo(0));
        assertThat(idx.getIndex(42), equalTo(0));
        assertThat(idx.getKey(0), equalTo(42L));
        try {
            idx.getIndex(30);
            fail("getting absent index should fail");
        } catch (IllegalArgumentException ex) {
            /* expected */
        }
    }

    @Test
    public void testFailsWithDuplicates() {
        LongList keys = new LongArrayList();
        keys.add(10);
        keys.add(42);
        keys.add(5);
        keys.add(10);
        try {
            KeyIndex idx = new FrozenHashKeyIndex(keys);
            fail("creating key index with duplicates should fail");
        } catch (IllegalArgumentException ex) {
            /* expected */
        }
    }
}
