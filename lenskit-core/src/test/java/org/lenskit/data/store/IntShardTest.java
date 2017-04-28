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

public class IntShardTest {
    @Test
    public void testInitialState() {
        IntShard shard = IntShard.createFull();
        assertThat(shard.size(), equalTo(0));
    }

    @Test
    public void testAddObject() {
        IntShard shard = IntShard.createFull();
        shard.put(0, 42);
        assertThat(shard.size(), equalTo(1));
        assertThat(shard.get(0), equalTo(42));
        assertThat(shard.isNull(0), equalTo(false));
    }

    @Test
    public void testAddObjectLater() {
        IntShard shard = IntShard.createFull();
        shard.put(5, 42);
        assertThat(shard.size(), equalTo(6));
        assertThat(shard.get(5), equalTo(42));
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.isNull(4), equalTo(true));
        assertThat(shard.isNull(5), equalTo(false));
    }

    @Test
    public void testClearObject() {
        IntShard shard = IntShard.createFull();
        shard.put(0, 42);
        shard.put(1, 39);
        shard.put(0, null);
        assertThat(shard.size(), equalTo(2));
        assertThat(shard.get(0), nullValue());
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.get(1), equalTo(39));
        assertThat(shard.isNull(1), equalTo(false));
    }

    @Test
    public void testWrapInitialState() {
        IntShard shard = IntShard.create();
        assertThat(shard.size(), equalTo(0));
    }

    @Test
    public void testWrapAddObject() {
        IntShard shard = IntShard.create();
        shard.put(0, 42);
        assertThat(shard.size(), equalTo(1));
        assertThat(shard.get(0), equalTo(42));
        assertThat(shard.isNull(0), equalTo(false));
    }

    @Test
    public void testWrapAddObjectLater() {
        IntShard shard = IntShard.create();
        shard.put(5, 42);
        assertThat(shard.size(), equalTo(6));
        assertThat(shard.get(5), equalTo(42));
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.isNull(4), equalTo(true));
        assertThat(shard.isNull(5), equalTo(false));
    }

    @Test
    public void testWrapClearObject() {
        IntShard shard = IntShard.create();
        shard.put(0, 42);
        shard.put(1, 39);
        shard.put(0, null);
        assertThat(shard.size(), equalTo(2));
        assertThat(shard.get(0), nullValue());
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.get(1), equalTo(39));
        assertThat(shard.isNull(1), equalTo(false));
    }

    @Test
    public void testAdaptUpgrade() {
        Shard shard = IntShard.create();
        shard.put(0, 42);
        shard = shard.adapt(Short.MAX_VALUE + 10);
        shard.put(1, Short.MAX_VALUE + 10);
        assertThat(shard.size(), equalTo(2));
        assertThat(shard.get(0), equalTo(42));
        assertThat(shard.isNull(0), equalTo(false));
        assertThat(shard.get(1), equalTo(Short.MAX_VALUE + 10));
        assertThat(shard.isNull(1), equalTo(false));
    }
}