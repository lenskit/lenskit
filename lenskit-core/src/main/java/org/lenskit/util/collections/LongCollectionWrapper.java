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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;

import java.util.Arrays;
import java.util.Collection;

/**
 * Wrapper class that implements a {@link LongCollection} by delegating to
 * a {@link Collection}.
 */
class LongCollectionWrapper implements LongCollection {
    protected final Collection<Long> base;

    LongCollectionWrapper(Collection<Long> b) {
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
    public LongIterator iterator() {
        return LongIterators.asLongIterator(base.iterator());
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

    /**
     * {@inheritDoc}
     *
     * @deprecated see {@link LongCollection#longIterator()}
     */
    @Override
    @Deprecated
    public LongIterator longIterator() {
        return iterator();
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
}
