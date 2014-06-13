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
package org.grouplens.lenskit.data.dao.packed;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.AbstractIntList;

import java.nio.IntBuffer;

/**
 * A list of integers in a buffer.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class BufferBackedIntList extends AbstractIntList {
    private final IntBuffer buffer;
    private final int offset;
    private final int size;

    private BufferBackedIntList(IntBuffer buf, int off, int sz) {
        buffer = buf;
        offset = off;
        size = sz;
    }

    /**
     * Create a new buffer-backed int list.  The list contains the integers from the position
     * to the limit of the list.
     * @param buf The buffer.
     * @param offset The offset into the buffer to start at.
     * @param size The number of items to use from the buffer.
     */
    public static BufferBackedIntList create(IntBuffer buf, int offset, int size) {
        assert offset >= 0;
        assert size >= 0;
        assert offset + size <= buf.limit();
        return new BufferBackedIntList(buf, offset, size);
    }


    /**
     * Create a new buffer-backed int list.  The list contains the integers from the position
     * to the limit of the list.
     * @param buf The buffer.
     */
    public static BufferBackedIntList create(IntBuffer buf) {
        return create(buf, buf.position(), buf.limit() - buf.position());
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int getInt(int i) {
        Preconditions.checkElementIndex(i, size());
        return buffer.get(offset + i);
    }
}
