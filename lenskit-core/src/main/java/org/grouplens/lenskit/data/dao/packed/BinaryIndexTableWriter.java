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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Write an index table and store.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class BinaryIndexTableWriter {
    private final BinaryFormat format;
    private final FileChannel channel;
    private final int entryCount;
    private final long tableStartPosition;
    private long currentEntryPosition;

    private ByteBuffer entryBuffer;
    private ByteBuffer storeBuffer;

    // current offset into the store.
    private int currentOffset = 0;

    private BinaryIndexTableWriter(BinaryFormat fmt, FileChannel chan, int nkeys) throws IOException {
        format = fmt;
        channel = chan;
        entryCount = nkeys;
        tableStartPosition = channel.position();
        currentEntryPosition = tableStartPosition;

        entryBuffer = ByteBuffer.allocateDirect(BinaryIndexTable.TABLE_ENTRY_SIZE);

        channel.position(tableStartPosition + nkeys * format.indexTableEntrySize());
    }

    public static BinaryIndexTableWriter create(BinaryFormat fmt, FileChannel chan, int nkeys) throws IOException {
        return new BinaryIndexTableWriter(fmt, chan, nkeys);
    }

    public void writeEntry(long id, int[] indexes) throws IOException {
        writeEntryHeader(id, indexes.length);

        int storeBytes = indexes.length * BinaryFormat.INT_SIZE;
        if (storeBuffer == null || storeBuffer.capacity() < storeBytes) {
            storeBuffer = ByteBuffer.allocateDirect(storeBytes);
        }
        assert storeBuffer.position() == 0;
        assert storeBuffer.limit() >= storeBytes;

        for (int idx: indexes) {
            storeBuffer.putInt(idx);
        }
        storeBuffer.flip();
        assert storeBuffer.limit() == storeBytes;

        BinaryUtils.writeBuffer(channel, storeBuffer);
        storeBuffer.clear();

        currentOffset += indexes.length;

        assert channel.position() == tableStartPosition
                                     + (entryCount * BinaryIndexTable.TABLE_ENTRY_SIZE)
                                     + (currentOffset * BinaryFormat.INT_SIZE);
    }

    private void writeEntryHeader(long id, int length) throws IOException {
        assert entryBuffer.position() == 0;
        entryBuffer.putLong(id);
        entryBuffer.putInt(currentOffset);
        entryBuffer.putInt(length);
        entryBuffer.flip();
        BinaryUtils.writeBuffer(channel, entryBuffer, currentEntryPosition);
        entryBuffer.clear();
        currentEntryPosition += BinaryIndexTable.TABLE_ENTRY_SIZE;
    }

    private void finish() throws IOException {

    }
}
