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
package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.*;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

/**
 * A long array list that can use compact storage.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class CompactableLongArrayList extends AbstractLongList implements RandomAccess, Serializable {
    private static final long serialVersionUID = 1L;

    // Invariant: exactly one of these is non-null, containing the implementation
    @Nullable
    private IntArrayList compactImpl;
    @Nullable
    private LongArrayList fullImpl;

    public CompactableLongArrayList() {
        this(LongArrayList.DEFAULT_INITIAL_CAPACITY);
    }

    public CompactableLongArrayList(int initialCapacity) {
        compactImpl = new IntArrayList(initialCapacity);
    }

    public CompactableLongArrayList(Collection<? extends Long> c) {
        this(c.size());
        addAll(c);
    }

    private CompactableLongArrayList(IntArrayList items) {
        compactImpl = items;
    }

    private List<? extends Number> activeImpl() {
        if (compactImpl != null) {
            assert fullImpl == null;
            return compactImpl;
        } else {
            assert fullImpl != null;
            return fullImpl;
        }
    }

    private void decompact() {
        assert compactImpl != null && fullImpl == null;
        fullImpl = new LongArrayList(compactImpl.elements().length);
        for (IntIterator iter = compactImpl.iterator(); iter.hasNext();) {
            final int value = iter.nextInt();
            fullImpl.add(value);
        }
        compactImpl = null;
    }

    private void ensureCanInsert(long val) {
        if (fullImpl == null && (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE)) {
            decompact();
        }
    }

    public void trim() {
        if (fullImpl != null) {
            boolean compactable = true;
            for (LongIterator iter = fullImpl.iterator(); compactable && iter.hasNext();) {
                final long value = iter.nextLong();
                if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                    compactable = false;
                }
            }
            if (compactable) {
                compactImpl = new IntArrayList(fullImpl.size());
                for (LongIterator iter = fullImpl.iterator(); iter.hasNext();) {
                    final long value = iter.nextLong();
                    compactImpl.add((int) value);
                }
                fullImpl = null;
            } else {
                fullImpl.trim();
            }
        } else {
            assert compactImpl != null;
            compactImpl.trim();
        }
    }

    @Override
    public int size() {
        return activeImpl().size();
    }

    @Override
    public boolean isEmpty() {
        return activeImpl().isEmpty();
    }

    @Override
    public long getLong(int i) {
        if (compactImpl != null) {
            return compactImpl.getInt(i);
        } else {
            assert fullImpl != null;
            return fullImpl.getLong(i);
        }
    }

    @Override
    public LongListIterator iterator() {
        if (fullImpl != null) {
            return fullImpl.iterator();
        } else {
            // delegate to superclass's index-based iterator
            return super.iterator();
        }
    }

    @Override
    public LongListIterator listIterator() {
        if (fullImpl != null) {
            return fullImpl.listIterator();
        } else {
            // delegate to superclass's index-based iterator
            return super.listIterator();
        }
    }

    @Override
    public LongListIterator listIterator(int index) {
        if (fullImpl != null) {
            return fullImpl.listIterator(index);
        } else {
            // delegate to superclass's index-based iterator
            return super.listIterator(index);
        }
    }

    @Override
    public void add(int index, long k) {
        ensureCanInsert(k);
        if (fullImpl != null) {
            fullImpl.add(index, k);
        } else {
            assert compactImpl != null;
            compactImpl.add(index, (int) k);
        }
    }

    @Override
    public boolean add(long k) {
        ensureCanInsert(k);
        if (fullImpl != null) {
            return fullImpl.add(k);
        } else {
            assert compactImpl != null;
            return compactImpl.add((int) k);
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends Long> c) {
        if (fullImpl != null) {
            return fullImpl.addAll(index, c);
        } else {
            return super.addAll(index, c);
        }
    }

    @Override
    public boolean addAll(Collection<? extends Long> c) {
        if (fullImpl != null) {
            return fullImpl.addAll(c);
        } else {
            return super.addAll(c);
        }
    }

    @Override
    public long set(int index, long k) {
        ensureCanInsert(k);
        if (fullImpl != null) {
            return fullImpl.set(index, k);
        } else {
            assert compactImpl != null;
            return compactImpl.set(index, (int) k);
        }
    }

    @Override
    public long removeLong(int index) {
        if (fullImpl != null) {
            return fullImpl.removeLong(index);
        } else {
            assert compactImpl != null;
            return compactImpl.removeInt(index);
        }
    }

    @Override
    public LongList subList(int from, int to) {
        if (fullImpl != null) {
            // drop indirection
            return fullImpl.subList(from, to);
        } else {
            return super.subList(from, to);
        }
    }

    private Object writeReplace() throws ObjectStreamException {
        if (fullImpl != null) {
            // just serialize the non-compact list
            return fullImpl;
        } else {
            // serialize the compact list in a proxy wrapper
            return new SerialProxy(compactImpl);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new InvalidObjectException("must use serialization proxy");
    }

    private static final class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final IntArrayList storage;

        public SerialProxy(IntArrayList items) {
            storage = items;
        }

        private Object readResolve() throws ObjectStreamException {
            return new CompactableLongArrayList(storage);
        }
    }
}
