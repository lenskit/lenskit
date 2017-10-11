/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
