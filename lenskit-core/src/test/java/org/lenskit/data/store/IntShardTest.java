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