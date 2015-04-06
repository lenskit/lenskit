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

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.collections.LongKeyDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * An index table from a byte buffer.  An index table maps IDs (user or item IDs) to the positions
 * in the rating store where their ratings are; it can be thought of as a map from long IDs to lists
 * of integer rating indexes.
 * <p>
 * An index table is organized as follows:
 * </p>
 * <ol>
 * <li>A 16-byte table, containing the long ID, the offset into the index store, and the number
 * of indexes.</li>
 * <li>An index store, a sequence of ints representing the actual storage.</li>
 * </ol>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
class BinaryIndexTable implements Serializable {
    static final int TABLE_ENTRY_SIZE = BinaryFormat.LONG_SIZE + 2 * BinaryFormat.INT_SIZE;

    private static final long serialVersionUID = -1L;
    private static final Logger logger = LoggerFactory.getLogger(BinaryIndexTable.class);
    /**
     * The keys of the index.
     */
    private final LongKeyDomain keys;
    /**
     * The location in the index store of each key's indexes.
     */
    private final int[] offsets;
    /**
     * The number of indexes associated with each key.
     */
    private final int[] sizes;
    /**
     * The index store.
     */
    private final IntBuffer indexStore;

    private BinaryIndexTable(LongKeyDomain keytbl, int[] offtbl, int[] sztbl, IntBuffer buf) {
        assert offtbl.length == keytbl.domainSize();
        assert sztbl.length == keytbl.domainSize();
        keys = keytbl;
        offsets = offtbl;
        sizes = sztbl;
        indexStore = buf;
    }

    /**
     * Create a binary index table.
     * @param nentries The number of entries in the table.
     * @param buffer The table buffer.  Its position will be advanced to the end of the table.
     * @return The index table.
     */
    public static BinaryIndexTable fromBuffer(int nentries, ByteBuffer buffer) {
        logger.debug("reading table of {} entries", nentries);
        long[] keys = new long[nentries];
        int[] offsets = new int[nentries];
        int[] sizes = new int[nentries];
        // Read the index table's header (IDs, offsets, and counts/sizes).
        int nextExpectedOffset = 0;
        for (int i = 0; i < nentries; i++) {
            keys[i] = buffer.getLong();
            if (i > 0 && keys[i-1] >= keys[i]) {
                logger.error("key {} is not greater than previous key {}", keys[i], keys[i-1]);
                throw new IllegalArgumentException("corrupted index table");
            }
            offsets[i] = buffer.getInt();
            sizes[i] = buffer.getInt();
            if (offsets[i] != nextExpectedOffset) {
                logger.error("expected offset {}, got {}", nextExpectedOffset, offsets[i]);
                throw new IllegalArgumentException("corrupted index table");
            }
            nextExpectedOffset += sizes[i];
        }

        // Set up the integer store
        if (buffer.remaining() < nextExpectedOffset * 4) {
            throw new IllegalArgumentException("buffer not large enough");
        }
        int end = buffer.position() + nextExpectedOffset * 4;
        ByteBuffer dup = buffer.duplicate();
        dup.limit(end);
        // update input indexStore's position
        buffer.position(end);
        // create index table object
        LongKeyDomain dom = LongKeyDomain.wrap(keys, keys.length, true);
        return new BinaryIndexTable(dom, offsets, sizes, dup.asIntBuffer());
    }

    public  BinaryIndexTable createLimitedView(int limit) {
        LongKeyDomain newKeys = keys.clone();
        int[] newSizes = new int[sizes.length];
        for (int i=0;i<offsets.length;i++) {
            if (indexStore.get(offsets[i])>=limit||sizes[i]==0) {
                newSizes[i]=0;
                newKeys.setActive(i, false);
            } else {
                //TODO following loop need to be replaced with binary rating search
                for (int j= offsets[i];j<(offsets[i]+sizes[i]);j++) {
                    /*
                    * find the new 'size' value; this is the number of indexes for this key that
                    * are less than the limit
                    */
                    if(indexStore.get(j)<limit)
                        newSizes[i]+=1;
                }
            }
        }
        return new BinaryIndexTable(newKeys,offsets, newSizes, indexStore);
    }
    public LongSet getKeys() {
        return keys.activeSetView();
    }

    /**
     * Get the position list for a key.
     * @param key The key.
     * @return The position list.
     */
    public IntList getEntry(long key) {
        int idx = keys.getIndexIfActive(key);
        if (idx < 0) {
            return null;
        }
        return getEntryInternal(idx);
    }

    private IntList getEntryInternal(int idx) {
        int offset = offsets[idx];
        int size = sizes[idx];
        IntBuffer buf = indexStore.duplicate();
        buf.position(offset).limit(offset + size);
        return BufferBackedIntList.create(buf);
    }

    public Collection<Pair<Long,IntList>> entries() {
        return new EntryCollection();
    }

    private Object writeReplace() throws ObjectStreamException {
        return new SerialProxy(keys, offsets, sizes, indexStore);
    }

    private Object readObject(ObjectInputStream in) throws IOException {
        throw new InvalidObjectException("index table must use serial proxy");
    }

    @SuppressWarnings("deprecation")
    private class EntryCollection extends AbstractCollection<Pair<Long, IntList>> {
        @Override
        public int size() {
            return keys.domainSize();
        }

        @Override
        @Nonnull
        public Iterator<Pair<Long, IntList>> iterator() {
            return new IterImpl();
        }
    }

    private class IterImpl implements Iterator<Pair<Long,IntList>> {
        int pos = 0;

        @Override
        public boolean hasNext() {
            return pos < keys.domainSize();
        }

        @Override
        public Pair<Long, IntList> next() {
            int i = pos;
            pos += 1;
            return Pair.of(keys.getKey(i), getEntryInternal(i));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 2L;

        private final LongKeyDomain keys;
        private final int[] offsets;
        private final int[] sizes;
        private transient IntBuffer buffer;

        private SerialProxy(LongKeyDomain keys, int [] offsets, int[] sizes, IntBuffer buffer) {
            this.keys = keys.clone();
            this.offsets = offsets;
            this.sizes = sizes;
            this.buffer = buffer.duplicate();
            this.buffer.clear();
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeInt(buffer.limit());
            while (buffer.hasRemaining()) {
                out.writeInt(buffer.get());
            }
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            int size = in.readInt();
            ByteBuffer store = ByteBuffer.allocateDirect(size * 4);
            buffer = store.asIntBuffer();
            assert buffer.remaining() == size;
            while (buffer.hasRemaining()) {
                buffer.put(in.readInt());
            }
            buffer.clear();
        }

        private Object readResolve() throws ObjectStreamException {
            if (keys.domainSize() != offsets.length || keys.domainSize() != sizes.length) {
                throw new InvalidObjectException("arrays not the same length");
            }
            return new BinaryIndexTable(keys, offsets, sizes, buffer.duplicate());
        }
    }
}
