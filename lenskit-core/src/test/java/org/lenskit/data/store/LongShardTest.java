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
package org.lenskit.data.store;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class LongShardTest {
    @Test
    public void testInitialState() {
        LongShard shard = LongShard.createFull();
        assertThat(shard.size(), equalTo(0));
    }

    @Test
    public void testAddObject() {
        LongShard shard = LongShard.createFull();
        shard.put(0, 42L);
        assertThat(shard.size(), equalTo(1));
        assertThat(shard.get(0), equalTo(42L));
        assertThat(shard.isNull(0), equalTo(false));
    }

    @Test
    public void testAddObjectLater() {
        LongShard shard = LongShard.createFull();
        shard.put(5, 42L);
        assertThat(shard.size(), equalTo(6));
        assertThat(shard.get(5), equalTo(42L));
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.isNull(4), equalTo(true));
        assertThat(shard.isNull(5), equalTo(false));
    }

    @Test
    public void testClearObject() {
        LongShard shard = LongShard.createFull();
        shard.put(0, 42L);
        shard.put(1, 39L);
        shard.put(0, null);
        assertThat(shard.size(), equalTo(2));
        assertThat(shard.get(0), nullValue());
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.get(1), equalTo(39L));
        assertThat(shard.isNull(1), equalTo(false));
    }

    @Test
    public void testShortWrapInitialState() {
        LongShard shard = LongShard.create();
        assertThat(shard.size(), equalTo(0));
    }

    @Test
    public void testShortWrapAddObject() {
        LongShard shard = LongShard.create();
        shard.put(0, 42L);
        assertThat(shard.size(), equalTo(1));
        assertThat(shard.get(0), equalTo(42L));
        assertThat(shard.isNull(0), equalTo(false));
    }

    @Test
    public void testShortWrapAddObjectLater() {
        LongShard shard = LongShard.create();
        shard.put(5, 42L);
        assertThat(shard.size(), equalTo(6));
        assertThat(shard.get(5), equalTo(42L));
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.isNull(4), equalTo(true));
        assertThat(shard.isNull(5), equalTo(false));
    }

    @Test
    public void testShortWrapClearObject() {
        LongShard shard = LongShard.create();
        shard.put(0, 42L);
        shard.put(1, 39L);
        shard.put(0, null);
        assertThat(shard.size(), equalTo(2));
        assertThat(shard.get(0), nullValue());
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.get(1), equalTo(39L));
        assertThat(shard.isNull(1), equalTo(false));
    }

    @Test
    public void testAdaptUpgrade() {
        Shard shard = LongShard.create();
        shard.put(0, 42L);
        shard = shard.adapt(Short.MAX_VALUE + 10L);
        shard.put(1, Short.MAX_VALUE + 10L);
        assertThat(shard.size(), equalTo(2));
        assertThat(shard.get(0), equalTo(42L));
        assertThat(shard.isNull(0), equalTo(false));
        assertThat(shard.get(1), equalTo(Short.MAX_VALUE + 10L));
        assertThat(shard.isNull(1), equalTo(false));
    }

    @Test
    public void testIntWrapInitialState() {
        LongShard shard = LongShard.create();
        assertThat(shard.size(), equalTo(0));
    }

    @Test
    public void testIntWrapAddObject() {
        LongShard shard = LongShard.create();
        shard.put(0, 42L);
        assertThat(shard.size(), equalTo(1));
        assertThat(shard.get(0), equalTo(42L));
        assertThat(shard.isNull(0), equalTo(false));
    }

    @Test
    public void testIntWrapAddObjectLater() {
        LongShard shard = LongShard.create();
        shard.put(5, 42L);
        assertThat(shard.size(), equalTo(6));
        assertThat(shard.get(5), equalTo(42L));
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.isNull(4), equalTo(true));
        assertThat(shard.isNull(5), equalTo(false));
    }

    @Test
    public void testIntWrapClearObject() {
        LongShard shard = LongShard.create();
        shard.put(0, 42L);
        shard.put(1, 39L);
        shard.put(0, null);
        assertThat(shard.size(), equalTo(2));
        assertThat(shard.get(0), nullValue());
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.get(1), equalTo(39L));
        assertThat(shard.isNull(1), equalTo(false));
    }

    @Test
    public void testLongAdaptUpgrade() {
        Shard shard = LongShard.create();
        shard = shard.adapt(42L);
        shard.put(0, 42L);
        shard = shard.adapt(Short.MAX_VALUE + 10L);
        shard.put(1, Short.MAX_VALUE + 10L);
        shard = shard.adapt(Integer.MAX_VALUE + 10L);
        shard.put(2, Integer.MAX_VALUE + 10L);
        assertThat(shard.size(), equalTo(3));
        assertThat(shard.get(0), equalTo(42L));
        assertThat(shard.isNull(0), equalTo(false));
        assertThat(shard.get(1), equalTo(Short.MAX_VALUE + 10L));
        assertThat(shard.isNull(1), equalTo(false));
        assertThat(shard.get(2), equalTo(Integer.MAX_VALUE + 10L));
        assertThat(shard.isNull(2), equalTo(false));
    }
}