package org.grouplens.lenskit.data.dao.packed;

import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BufferBackedIntListTest {
    @Test
    public void testEmptyBuffer() {
        IntList list = BufferBackedIntList.create(ByteBuffer.allocate(0));
        assertThat(list, hasSize(0));
    }

    @Test
    public void testSingletonBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(42);
        buf.flip();
        IntList list = BufferBackedIntList.create(buf);
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
        IntList list = BufferBackedIntList.create(buf);
        assertThat(list, hasSize(5));
        assertThat(list, contains(0, 1, 2, 3, 4));
    }
}
