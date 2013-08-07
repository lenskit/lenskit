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

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.objects.*;
import org.grouplens.lenskit.collections.LongSortedArraySet;

import java.util.Arrays;
import java.util.NoSuchElementException;

class TypedSideChannel<V> extends AbstractLong2ObjectMap<V> {
    private static final long serialVersionUID = 1L;
    
    protected final long[] keys;
    protected V[] values;
    protected final int domainSize; // How much of the key space is actually used by this vector.
    protected boolean frozen = false;
    
    /**
     * Build a new TypedSideChannel from the given keys, it is assumed that the key array is sorted, 
     * duplicate free. The key array will be the key domain.
     * @param ks The array of keys backing this vector. They must be sorted.
     */
    TypedSideChannel(long[] ks) {
        this(ks, ks.length);
    }
    
    /**
     * Build a new TypedSideChannel from the given keys and values.
     * It is assumed that the key array is sorted, duplicate free, and the same length as the value
     * array. The key array will become the key domain, the value array will become the values, and
     * that every key has a value. 
     * 
     * @param ks The array of keys backing this vector. They must be sorted.
     * @param vs The array of values backing this vector.
     */
    TypedSideChannel(long[] ks, V[] vs) {
        this(ks, vs, ks.length);
    }
    
    /**
     * Build a new TypedSideChannel from the given keys and values using only the first 
     * {@param length} values. It is assumed that both key and value has at least {@param length} items.
     * It is assumed that the key array is sorted, duplicate free, and the same length as the value
     * array. The key array will become the key domain, the value array will become the values, and
     * that every key has a value. 
     * 
     * @param ks The array of keys backing this vector. They must be sorted.
     * @param vs The array of values backing this vector.
     * @param length Number of items to actually use.
     */
    TypedSideChannel(long[] ks, V[] vs, int length) {
        keys = ks;
        values = vs;
        domainSize = length;
    }
    
    /**
     * Build a new TypedSideChannel from the given keys using only the first {@param length} values.
     * It is assumed that both ks has at least {@param length} items.
     * It is assumed that the key array is sorted, duplicate free The key array will become the 
     * key domain. 
     * 
     * @param keys The array of keys backing this vector. They must be sorted.
     * @param length Number of items to actually use.
     */
    @SuppressWarnings("unchecked")
    TypedSideChannel(long[] keys, int length) {
        this(keys, (V[])new Object[length], length);
    }
    
    @Override
    public ObjectSet<Long2ObjectMap.Entry<V>> long2ObjectEntrySet() {
        return new EntrySetImpl();
    }

    private class EntrySetImpl extends AbstractObjectSet<Entry<V>> implements FastEntrySet<V> {
        @Override
        public ObjectIterator<Entry<V>> iterator() {
            return new AbstractObjectIterator<Entry<V>>() {
                int next;
                {
                    next = 0;
                    while (next < domainSize && values[next] == null) {
                        next++;
                    }
                }

                @Override
                public boolean hasNext() {
                    return next < domainSize;
                }

                @Override
                public Entry<V> next() {
                    if (next >= domainSize) {
                        throw new NoSuchElementException();
                    }
                    Entry<V> e = new EntryImpl(next);
                    next++;
                    while (next < domainSize && values[next] == null) {
                        next++;
                    }
                    return e;
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
                EntryImpl entry = new EntryImpl();
                int next;
                {
                    next = 0;
                    while (next < domainSize && values[next] == null) {
                        next++;
                    }
                }

                @Override
                public boolean hasNext() {
                    return next < domainSize;
                }

                @Override
                public Entry<V> next() {
                    if (next >= domainSize) {
                        throw new NoSuchElementException();
                    }
                    entry.setIndex(next);
                    next++;
                    while (next < domainSize && values[next] == null) {
                        next++;
                    }
                    return entry;
                }
            };
        }
    }

    private class EntryImpl implements Entry<V> {
        private int index;

        private EntryImpl() {
            index = 0;
        }
        private EntryImpl(int idx) {
            index = idx;
        }

        private void setIndex(int idx) {
            index = idx;
        }

        @Override
        public long getLongKey() {
            return keys[index];
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

    /**
     * Find the index of a particular key.
     *
     * @param key The key to search for.
     * @return The index, or a negative value if the key is not in the key domain.
     */
    protected int findIndex(long key) {
        return Arrays.binarySearch(keys, 0, domainSize, key);
    }
    
    @Override
    public boolean containsKey(long key) {
        final int idx = findIndex(key);
        return idx >= 0 && values[idx] != null;
    }
    
    @Override
    public boolean containsValue(Object v) {
        return ObjectArrayList.wrap(values, domainSize).contains(v);
    }
    
    @Override
    public V get(long key) {
        final int idx = findIndex(key);
        if (idx >= 0 && values[idx] != null) {
            return values[idx];
        } else {
            return defaultReturnValue();
        }
    }

    /**
     * Get the value associated with a particular vector entry in this side channel.
     * @param entry The entry. It must come from a vector to which this is a side channel.
     * @return The value, or {@link #defaultReturnValue()} if the channel is unset for the entry.
     * @throws IllegalArgumentException if the entry comes from an unrelated vector.
     */
    public V get(VectorEntry entry) {
        final SparseVector evec = entry.getVector();
        final int eind = entry.getIndex();

        if (evec == null) {
            throw new IllegalArgumentException("entry is not associated with a vector");
        } else if (evec.keys != this.keys) {
            throw new IllegalArgumentException("entry does not have safe key domain");
        } else if (entry.getKey() != keys[eind]) {
            throw new IllegalArgumentException("entry does not have the correct key for its index");
        }
        V obj = values[eind];
        if (obj != null) {
            return obj;
        } else {
            return defaultReturnValue();
        }
    }

    /**
     * Determine whether this side channel is set for the specified vector entry.
     * @param entry The vector entry.
     * @return {@code true} if the side channel is set.
     */
    public boolean isSet(VectorEntry entry) {
        final SparseVector evec = entry.getVector();
        final int eind = entry.getIndex();

        if (evec == null) {
            throw new IllegalArgumentException("entry is not associated with a vector");
        } else if (evec.keys != this.keys) {
            throw new IllegalArgumentException("entry does not have safe key domain");
        } else if (entry.getKey() != keys[eind]) {
            throw new IllegalArgumentException("entry does not have the correct key for its index");
        }
        return values[eind] != null;
    }

    @Override
    public int size() {
        return FluentIterable.from(ObjectArrayList.wrap(values, domainSize))
                             .filter(Predicates.notNull())
                             .size();
    }
    
    @Override
    public void clear() {
        checkMutable();
        ObjectArrays.fill(values, null);
    }
    
    @Override
    public V put(long key, V value) {
        checkMutable();
        final int idx = findIndex(key);
        if(idx >= 0) {
            V retval = values[idx];
            values[idx] = value;
            return retval;
        } else {
            throw new IllegalArgumentException("Cannot set a key that is not in the domain.  key="
                    + key);
        }
    }
    
    @Override
    public V remove(long key) {
        checkMutable();
        final int idx = findIndex(key);
        V retval = get(key);
        if(idx >= 0) {
            values[idx] = null;
        }
        return retval;
    }
    
    @Override
    public void defaultReturnValue(V rv) {
        checkMutable();
        super.defaultReturnValue(rv);
    }
    
    /**
     * Creates a mutable copy of this side channel. The returned object can be modified without
     * modifying this side channel. The returned copy is shallow, and will share instances with 
     * this side channel.
     */
    public TypedSideChannel<V> mutableCopy() {
        return new TypedSideChannel<V>(keys, Arrays.copyOf(values, domainSize), domainSize);
    }
    
    /**
     * Creates an immutable copy of this side channel. This object can be modified without modifying 
     * the returned side channel. The returned copy is shallow, and will share instances with 
     * this side channel.
     */
    public ImmutableTypedSideChannel<V> immutableCopy() {
        return new ImmutableTypedSideChannel<V>(keys, Arrays.copyOf(values, domainSize), 
                                                domainSize);
    }
    
    /**
     * Creates an immutable copy of this side channel. This object cannot be modified without modifying 
     * the returned side channel. The returned copy is shallow, and will share instances with 
     * this side channel. This object will be invalidated after calling this.
     */
    public ImmutableTypedSideChannel<V> freeze() {
        frozen=true;
        return new ImmutableTypedSideChannel<V>(keys, values, domainSize);
    }
    
    /**
     * Check if this vector is Mutable.
     */
    protected void checkMutable() {
        if (frozen) {
            throw new IllegalStateException("side channel is frozen");
        }
    }
    
    /**
     * used to mark a sidechannel as frozen without all the busywork of freezing it.
     * Currently used for returning sidechanels from frozen sets.
     */
    TypedSideChannel<V> partialFreeze() {
        frozen = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    public TypedSideChannel<V> withDomain(LongSet domain) {
        LongSortedArraySet set;
        // since LSAS is immutable, we'll use its array if we can!
        if (domain instanceof LongSortedArraySet) {
            set = (LongSortedArraySet) domain;
        } else {
            set = new LongSortedArraySet(domain);
        }
        long[] ks = set.unsafeArray();
        V vals[] = (V[]) new Object[keys.length];

        int i = 0;
        int j = 0;
        while(i<domain.size() && j<domainSize) {
            if (ks[i] == keys[j]) {
                vals[i] = values[j];
                i = i + 1;
                j = j + 1;
            } else if (ks[i] < keys[j]) {
                i = i + 1;
            } else {
                j = j + 1;
            }
        }
        
        TypedSideChannel<V> retval = new TypedSideChannel<V>(ks, vals, domain.size());
        return retval;
    }
    
    /**
     * Get the key domain for this vector. All keys used are in this
     * set.  The keys will be in sorted order.
     *
     * @return The key domain for this vector.
     */
    public LongSortedSet keyDomain() {
        return LongSortedArraySet.wrap(keys, domainSize);
    }
}
