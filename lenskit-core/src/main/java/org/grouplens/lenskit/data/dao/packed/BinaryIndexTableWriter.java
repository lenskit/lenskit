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
