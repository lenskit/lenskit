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
package org.grouplens.lenskit.data.dao.packed;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Write an index table.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class BinaryIndexTableWriter implements Closeable {
    private final BinaryFormat format;
    private final ByteBuffer buffer;
    private final int entryCount;

    private ByteBuffer headerTable;
    private IntBuffer intStore;

    // current offset into the store.
    private int currentOffset = 0;

    private BinaryIndexTableWriter(BinaryFormat fmt, ByteBuffer buf, int nkeys) {
        format = fmt;
        buffer = buf;
        entryCount = nkeys;

        int tableSize = nkeys * BinaryIndexTable.TABLE_ENTRY_SIZE;

        ByteBuffer tmp = (ByteBuffer) buf.duplicate()
                                         .limit(tableSize);
        headerTable = tmp.slice();
        tmp = (ByteBuffer) buf.duplicate().position(tableSize);
        intStore = tmp.slice().asIntBuffer();
    }

    /**
     * Compute the size (in bytes) of an index table that will store the specified number of keys
     * and indexes.
     * @param keyCount The number of keys to be stored.
     * @param indexCount The total number of indexes to be stored.
     * @return
     */
    public static int computeSize(int keyCount, int indexCount) {
        return keyCount * BinaryIndexTable.TABLE_ENTRY_SIZE + indexCount * BinaryFormat.INT_SIZE;
    }

    public static BinaryIndexTableWriter create(BinaryFormat fmt, ByteBuffer buf, int nkeys) {
        return new BinaryIndexTableWriter(fmt, buf, nkeys);
    }

    /**
     * Create a table writer that writes to a file.
     * @param fmt The binary format.
     * @param chan The output file.  The table will be written to the file's current position.
     * @param nkeys The number of keys.
     * @param nidxes The total number of indexes to write.
     * @return
     * @throws IOException
     */
    public static BinaryIndexTableWriter create(BinaryFormat fmt, FileChannel chan, int nkeys, int nidxes) throws IOException {
        int size = computeSize(nkeys, nidxes);
        MappedByteBuffer buf = chan.map(FileChannel.MapMode.READ_WRITE,
                                        chan.position(), chan.position() + size);
        chan.position(chan.position() + size);
        return new BinaryIndexTableWriter(fmt, buf, nkeys);
    }

    /**
     * Write an entry into the index table.
     * @param id The ID.
     * @param indexes The indexes to store.
     * @throws IOException if there is an I/O error
     */
    public void writeEntry(long id, int[] indexes) {
        headerTable.putLong(id);
        headerTable.putInt(currentOffset);
        headerTable.putInt(indexes.length);

        for (int idx: indexes) {
            intStore.put(idx);
        }

        currentOffset += indexes.length;
    }

    @Override
    public void close() throws IOException {
        if (buffer instanceof MappedByteBuffer) {
            ((MappedByteBuffer) buffer).force();
        }
    }
}
