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
