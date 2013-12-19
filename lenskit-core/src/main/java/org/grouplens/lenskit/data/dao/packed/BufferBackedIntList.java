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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.AbstractIntList;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * A list of integers in a buffer.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BufferBackedIntList extends AbstractIntList {
    private final IntBuffer buffer;

    private BufferBackedIntList(IntBuffer buf) {
        buffer = buf;
    }

    /**
     * Create a new buffer-backed int list.  The list contains the integers from the position
     * to the limit of the list.
     * @param buf The buffer.  It is duplicated, so you can modify its position and limit later (but
     *            modifying its content will change the content of the list!)
     */
    public static BufferBackedIntList create(ByteBuffer buf) {
        return new BufferBackedIntList(buf.asIntBuffer());
    }

    /**
     * Create a new buffer-backed int list.  The list contains the integers from the position
     * to the limit of the list.
     * @param buf The buffer.  It is duplicated, so you can modify its position and limit later (but
     *            modifying its content will change the content of the list!)
     */
    public static BufferBackedIntList create(IntBuffer buf) {
        return new BufferBackedIntList(buf.slice());
    }

    @Override
    public int size() {
        return buffer.limit();
    }

    @Override
    public int getInt(int i) {
        Preconditions.checkElementIndex(i, size());
        return buffer.get(i);
    }
}
