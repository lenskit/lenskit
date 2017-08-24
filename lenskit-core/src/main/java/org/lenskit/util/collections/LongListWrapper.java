/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.longs.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Wrapper class that implements a {@link LongCollection} by delegating to
 * a {@link Collection}.
 */
class LongListWrapper extends AbstractLongList {
    protected final List<Long> base;

    LongListWrapper(List<Long> b) {
        base = b;
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean contains(long key) {
        return base.contains(key);
    }

    @Override
    public boolean add(Long item) {
        return base.add(item);
    }

    @Override
    public boolean addAll(Collection<? extends Long> items) {
        return base.addAll(items);
    }

    @Override
    public void clear() {
        base.clear();
    }

    @Override
    public boolean contains(Object item) {
        return base.contains(item);
    }

    @Override
    public boolean containsAll(Collection<?> items) {
        return base.containsAll(items);
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean remove(Object item) {
        return base.remove(item);
    }

    @Override
    public boolean removeAll(Collection<?> items) {
        return base.removeAll(items);
    }

    @Override
    public boolean retainAll(Collection<?> items) {
        return base.retainAll(items);
    }

    @Override
    public Object[] toArray() {
        return base.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return base.toArray(a);
    }

    @Override
    public long[] toLongArray() {
        final long[] items = new long[size()];
        LongIterators.unwrap(iterator(), items);
        return items;
    }

    @Override
    public long[] toLongArray(long[] a) {
        long[] output = a;
        if (output.length < size()) {
            output = new long[size()];
        }
        final int sz = LongIterators.unwrap(iterator(), output);
        if (sz < output.length) {
            output = Arrays.copyOf(output, sz);
        }
        return output;
    }

    @Override
    public long[] toArray(long[] a) {
        return toLongArray(a);
    }

    @Override
    public boolean add(long key) {
        return base.add(key);
    }

    @Override
    public boolean rem(long key) {
        return base.remove(key);
    }

    @Override
    public boolean addAll(LongCollection c) {
        return base.addAll(c);
    }

    @Override
    public boolean containsAll(LongCollection c) {
        return base.containsAll(c);
    }

    @Override
    public boolean removeAll(LongCollection c) {
        return base.removeAll(c);
    }

    @Override
    public boolean retainAll(LongCollection c) {
        return base.retainAll(c);
    }

    @Override
    public LongListIterator listIterator() {
        return new IterImpl(base.listIterator());
    }

    @Override
    public LongListIterator listIterator(int i) {
        return new IterImpl(base.listIterator(i));
    }

    @Override
    public LongList subList(int i, int i1) {
        return new LongListWrapper(base.subList(i, i1));
    }

    @Override
    public long getLong(int i) {
        return base.get(i);
    }

    @Override
    public int indexOf(long l) {
        return base.indexOf(l);
    }

    @Override
    public int lastIndexOf(long l) {
        return base.lastIndexOf(l);
    }

    @Override
    public long removeLong(int i) {
        return base.remove(i);
    }

    @Override
    public long set(int i, long l) {
        Long old = base.set(i, l);
        if (old == null) {
            return 0;
        } else {
            return old;
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends Long> c) {
        return base.addAll(index, c);
    }

    @Override
    public Long get(int index) {
        return base.get(index);
    }

    @Override
    public Long set(int index, Long element) {
        return base.set(index, element);
    }

    @Override
    public void add(int index, Long element) {
        base.add(index, element);
    }

    @Override
    public Long remove(int index) {
        return base.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return base.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return base.lastIndexOf(o);
    }

    private static class IterImpl extends AbstractLongListIterator {
        private final ListIterator<Long> delegate;

        public IterImpl(ListIterator<Long> iter) {
            delegate = iter;
        }

        @Override
        public void set(long l) {
            delegate.set(l);
        }

        @Override
        public void add(long l) {
            delegate.add(l);
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Long next() {
            return delegate.next();
        }

        @Override
        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        @Override
        public Long previous() {
            return delegate.previous();
        }

        @Override
        public int nextIndex() {
            return delegate.nextIndex();
        }

        @Override
        public int previousIndex() {
            return delegate.previousIndex();
        }

        @Override
        public void remove() {
            delegate.remove();
        }

        @Override
        public void set(Long aLong) {
            delegate.set(aLong);
        }

        @Override
        public void add(Long aLong) {
            delegate.add(aLong);
        }

        @Override
        public long previousLong() {
            return delegate.previous();
        }

        @Override
        public long nextLong() {
            return delegate.next();
        }
    }
}
