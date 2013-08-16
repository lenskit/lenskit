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
package org.grouplens.lenskit.vectors;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.grouplens.lenskit.collections.LongKeyDomain;

import java.util.Arrays;

class TypedSideChannel<V> extends AbstractLong2ObjectMap<V> {
    private static final long serialVersionUID = 1L;
    
    protected final LongKeyDomain keys;
    protected V[] values;
    protected boolean frozen = false;
    
    /**
     * Build a new TypedSideChannel from the given key set.
     * @param ks The key set backing this channel.  The key set will be referenced and modified,
     *           not copied.  Its mask is ignored (all keys will be initially deactivated).
     */
    TypedSideChannel(LongKeyDomain ks) {
        this(ks, (V[]) new Object[ks.domainSize()]);
        ks.setAllActive(false);
    }

    TypedSideChannel(LongKeyDomain ks, V[] vs) {
        this(ks, vs, null);
    }
    
    /**
     * Build a new TypedSideChannel from the given keys and values.
     * It is assumed that the key array is sorted, duplicate free, and the same length as the value
     * array. The key array will become the key domain, the value array will become the values, and
     * that every key has a value. 
     * 
     * @param ks The key set backing this vector.
     * @param vs The array of values backing this vector.
     * @param def The default return value.
     */
    TypedSideChannel(LongKeyDomain ks, V[] vs, V def) {
        assert vs.length >= ks.domainSize();
        keys = ks;
        values = vs;
        defRetValue = def;
    }
    
    @Override
    public ObjectSet<Long2ObjectMap.Entry<V>> long2ObjectEntrySet() {
        return new EntrySetImpl();
    }

    @Override
    public boolean containsValue(Object o) {
        // we have to override this method to avoid circular loops
        // prohibit nulls
        if (o == null) {
            return false;
        }
        // and search
        IntIterator iter = keys.activeIndexIterator();
        while (iter.hasNext()) {
            if (o.equals(values[iter.nextInt()])) {
                return true;
            }
        }
        return false;
    }

    private class EntrySetImpl extends AbstractObjectSet<Entry<V>> implements FastEntrySet<V> {
        @Override
        public ObjectIterator<Entry<V>> iterator() {
            return new AbstractObjectIterator<Entry<V>>() {
                IntIterator iter = keys.activeIndexIterator();

                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public Entry<V> next() {
                    int idx = iter.nextInt();
                    return new EntryImpl(idx);
                }
            };
        }

        @Override
        public int size() {
            return Iterators.size(iterator());
        }

        @Override
        public ObjectIterator<Entry<V>> fastIterator() {
            return new AbstractObjectIterator<Entry<V>>() {
                IntIterator iter = keys.activeIndexIterator();
                EntryImpl entry = new EntryImpl(-1);

                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public Entry<V> next() {
                    entry.setIndex(iter.nextInt());
                    return entry;
                }
            };
        }
    }

    private class EntryImpl implements Entry<V> {
        private int index;

        private EntryImpl(int idx) {
            assert idx < 0 || keys.indexIsActive(idx);
            index = idx;
        }

        private void setIndex(int idx) {
            assert keys.indexIsActive(idx);
            index = idx;
        }

        @Override
        public long getLongKey() {
            return keys.getKey(index);
        }

        @Override
        public Long getKey() {
            return getLongKey();
        }

        @Override
        public V getValue() {
            return values[index];
        }

        @Override
        public V setValue(V value) {
            V old = values[index];
            values[index] = value;
            return old;
        }
    }

    @Override
    public boolean containsKey(long key) {
        return keys.keyIsActive(key);
    }
    
    @Override
    public V get(long key) {
        final int idx = keys.getIndexIfActive(key);
        if (idx >= 0) {
            return values[idx];
        } else {
            return defaultReturnValue();
        }
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public void defaultReturnValue(V obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a mutable copy of this side channel. The returned object can be modified without
     * modifying this side channel. The returned copy is shallow, and will share instances with 
     * this side channel.
     */
    public MutableTypedSideChannel<V> mutableCopy() {
        return new MutableTypedSideChannel<V>(keys.clone(), Arrays.copyOf(values, keys.domainSize()),
                                              defaultReturnValue());
    }
    
    /**
     * Get an immutable version of this side channel.
     */
    public TypedSideChannel<V> immutable() {
        return this;
    }
}
