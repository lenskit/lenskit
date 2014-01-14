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
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.collections.CopyingFastCollection;
import org.grouplens.lenskit.collections.LongKeyDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Iterator;

/**
 * An index table from a byte buffer.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
class BinaryIndexTable implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(BinaryIndexTable.class);
    private final LongKeyDomain keys;
    private final int[] offsets;
    private final int[] sizes;
    private final IntBuffer buffer;

    private BinaryIndexTable(LongKeyDomain keytbl, int[] offtbl, int[] sztbl, IntBuffer buf) {
        keys = keytbl;
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
    public static BinaryIndexTable fromBuffer(int nentries, ByteBuffer buffer) {
        logger.debug("reading table of {} entries", nentries);
        long[] keys = new long[nentries];
        int[] offsets = new int[nentries];
        int[] sizes = new int[nentries];
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
        if (buffer.remaining() < nextExpectedOffset) {
            throw new IllegalArgumentException("buffer not large enough");
        }
        int end = buffer.position() + nextExpectedOffset * 4;
        ByteBuffer dup = buffer.duplicate();
        dup.limit(end);
        buffer.position(end);
        LongKeyDomain dom = LongKeyDomain.wrap(keys, keys.length, true);
        return new BinaryIndexTable(dom, offsets, sizes, dup.asIntBuffer());
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
        int idx = keys.getIndex(key);
        if (idx < 0) {
            return null;
        }

        return getEntryInternal(idx);
    }

    private IntList getEntryInternal(int idx) {
        int offset = offsets[idx];
        int size = sizes[idx];
        IntBuffer buf = buffer.duplicate();
        buf.position(offset).limit(offset + size);
        return BufferBackedIntList.create(buf);
    }

    public Collection<Pair<Long,IntList>> entries() {
        return new EntryCollection();
    }

    private class EntryCollection extends CopyingFastCollection<Pair<Long,IntList>> {
        @Override
        public int size() {
            return keys.domainSize();
        }

        @Override
        public Iterator<Pair<Long, IntList>> fastIterator() {
            return new FastIterImpl();
        }

        @Override
        protected Pair<Long, IntList> copy(Pair<Long, IntList> elt) {
            return Pair.of(elt.getLeft(), elt.getRight());
        }
    }

    private class FastIterImpl implements Iterator<Pair<Long,IntList>> {
        int pos = 0;
        MutablePair<Long,IntList> pair = new MutablePair<Long, IntList>();

        @Override
        public boolean hasNext() {
            return pos < keys.domainSize();
        }

        @Override
        public Pair<Long, IntList> next() {
            int i = pos;
            pos += 1;
            pair.setLeft(keys.getKey(i));
            pair.setRight(getEntryInternal(i));
            return pair;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
