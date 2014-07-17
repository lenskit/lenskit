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
package org.grouplens.lenskit.vectors;

import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.*;
import org.grouplens.lenskit.collections.CollectionUtils;

import java.util.Iterator;

/**
 * Shim implementing {@link it.unimi.dsi.fastutil.longs.Long2ObjectMap} on top of a {@link SparseVector}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class SparseVectorMap extends AbstractLong2ObjectMap<Double> {
    final SparseVector vector;

    /**
     * Create a new shim.
     * @param vec The vector to wrap.
     */
    SparseVectorMap(SparseVector vec) {
        vector = vec;
    }

    SparseVector getVector() {
        return vector;
    }

    @Override
    public ObjectSet<Entry<Double>> long2ObjectEntrySet() {
        return new EntrySetImpl();
    }

    @Override
    public LongSet keySet() {
        return vector.keySet();
    }

    @Override
    public ObjectCollection<Double> values() {
        return CollectionUtils.objectCollection(vector.values());
    }

    @Override
    public Double get(long key) {
        if (vector.containsKey(key)) {
            return vector.get(key);
        } else {
            return defaultReturnValue();
        }
    }

    @Override
    public boolean containsKey(long key) {
        return vector.containsKey(key);
    }

    @Override
    public int size() {
        return vector.size();
    }

    /**
     * Implement the entry set.
     */
    class EntrySetImpl extends AbstractObjectSet<Entry<Double>> implements FastEntrySet<Double> {
        @Override
        public ObjectIterator<Entry<Double>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return vector.size();
        }

        @Override
        public ObjectIterator<Entry<Double>> fastIterator() {
            return new FastEntryIterator();
        }
    }

    /**
     * Implement a map entry iterator.
     */
    class EntryIterator extends AbstractObjectIterator<Entry<Double>> {
        Iterator<VectorEntry> delegate = vector.fastIterator();

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Entry<Double> next() {
            VectorEntry e = delegate.next();
            return new BasicEntry<Double>(e.getKey(), (Double) e.getValue());
        }
    }

    /**
     * Implement a fast map iterator.
     */
    class FastEntryIterator extends AbstractObjectIterator<Entry<Double>> {
        Iterator<VectorEntry> delegate = vector.fastIterator();
        EntryShim entry = new EntryShim(null);

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Entry<Double> next() {
            VectorEntry e = delegate.next();
            entry.setEntry(e);
            return entry;
        }
    }

    /**
     * Implement a simple entry shim on top of a vector entry.
     */
    static class EntryShim implements Entry<Double> {
        VectorEntry entry;

        public EntryShim(VectorEntry e) {
            entry = e;
        }

        void setEntry(VectorEntry e) {
            entry = e;
        }

        @Override
        public long getLongKey() {
            return entry.getKey();
        }

        @Override
        public Long getKey() {
            return entry.getKey();
        }

        @Override
        public Double getValue() {
            return entry.getValue();
        }

        @Override
        public Double setValue(Double value) {
            throw new UnsupportedOperationException("setValue not supported");
        }
    }
}
