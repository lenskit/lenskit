/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.data.dao.packed;

import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BufferBackedIntListTest {
    @Test
    public void testEmptyBuffer() {
        IntList list = BufferBackedIntList.create(IntBuffer.allocate(0));
        assertThat(list, hasSize(0));
    }

    @Test
    public void testSingletonBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(42);
        buf.flip();
        IntList list = BufferBackedIntList.create(buf.asIntBuffer());
        assertThat(list, hasSize(1));
        assertThat(list, contains(42));
    }

    @Test
    public void testFiveElementBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(32);
        for (int i = 0; i < 5; i++) {
            buf.putInt(i);
        }
        buf.flip();
        IntList list = BufferBackedIntList.create(buf.asIntBuffer());
        assertThat(list, hasSize(5));
        assertThat(list, contains(0, 1, 2, 3, 4));
    }
}
