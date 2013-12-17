package org.grouplens.lenskit.data.dao.packed;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.ErrorManager;

/**
 * An index table from a byte buffer.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
public class BinaryIndexTable {
    private static final Logger logger = LoggerFactory.getLogger(BinaryIndexTable.class);
    private final Long2IntMap offsets;
    private final Long2IntMap sizes;
    private final IntBuffer buffer;

    private BinaryIndexTable(Long2IntMap offtbl, Long2IntMap sztbl, IntBuffer buf) {
        offsets = offtbl;
        sizes = sztbl;
        buffer = buf;
    }

    /**
     * Create a binary index table.
     * @param nentries The number of entries in the table.
     * @param buffer The table buffer.  Its position will be advanced to the end of the table.
     * @return The index table.
     */
    public static BinaryIndexTable create(int nentries, ByteBuffer buffer) {
        logger.debug("reading table of {} entries", nentries);
        Long2IntMap offsets = new Long2IntOpenHashMap(nentries);
        offsets.defaultReturnValue(-1);
        Long2IntMap sizes = new Long2IntOpenHashMap(nentries);
        int nextExpectedOffset = 0;
        for (int i = 0; i < nentries; i++) {
            long key = buffer.getLong();
            int off = buffer.getInt();
            int size = buffer.getInt();
            if (off != nextExpectedOffset) {
                logger.error("expected offset {}, got {}", nextExpectedOffset, off);
                throw new IllegalArgumentException("corrupted index table");
            }
            offsets.put(key, off);
            sizes.put(key, size);
            nextExpectedOffset += size;
        }
        if (buffer.remaining() < nextExpectedOffset) {
            throw new IllegalArgumentException("buffer not large enough");
        }
        int end = buffer.position() + nextExpectedOffset * 4;
        ByteBuffer dup = buffer.duplicate();
        dup.limit(end);
        buffer.position(end);
        return new BinaryIndexTable(offsets, sizes, dup.asIntBuffer());
    }

    /**
     * Return the space taken by this buffer, in bytes.
     * @return The space (in bytes) taken by this buffer.
     */
    public int getTableSize() {
        return offsets.size() * (8 + 4 + 4) + buffer.capacity() * 4;
    }

    public LongSet getKeys() {
        return offsets.keySet();
    }

    /**
     * Get the position list for a key.
     * @param key The key.
     * @return The position list.
     */
    public IntList getEntry(long key) {
        int off = offsets.get(key);
        if (off < 0) {
            return null;
        }

        IntBuffer buf = buffer.duplicate();
        buf.position(off).limit(off + sizes.get(key));
        return BufferBackedIntList.create(buf);
    }
}
