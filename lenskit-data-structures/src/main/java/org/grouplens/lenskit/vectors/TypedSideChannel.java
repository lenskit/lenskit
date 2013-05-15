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

import java.util.Arrays;
import java.util.BitSet;

import org.grouplens.lenskit.collections.BitSetIterator;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

class TypedSideChannel<V> extends AbstractLong2ObjectMap<V> {
    private static final long serialVersionUID = 1L;
    
    protected final long[] keys;
    protected final BitSet usedKeys;
    protected V[] values;
    protected final int domainSize; // How much of the key space is actually used by this vector.
    
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
        this(ks, vs, new BitSet(length), length);
        usedKeys.set(0, length);

    }
    
    /**
     * Build a new TypedSideChannel from the given keys using only the first {@param length} values.
     * It is assumed that both ks has at least {@param length} items.
     * It is assumed that the key array is sorted, duplicate free The key array will become the 
     * key domain. 
     * 
     * @param ks The array of keys backing this vector. They must be sorted.
     * @param length Number of items to actually use.
     */
    @SuppressWarnings("unchecked")
    TypedSideChannel(long[] keys, int length) {
        this(keys, (V[])new Object[length], new BitSet(length), length);
    }
    
    /**
     * Build a new TypedSideChannel from the given keys, values, and bitSit.
     * It is assumed that the keys and values array are the same length as the bitSet, 
     * that the keys are sorted and duplicate free.
     * The keys array will become the key set.
     */
    TypedSideChannel(long[] ks, V[] vs, BitSet bs) {
        this(ks, vs, bs, bs.length());
    }
    
    /**
     * Build a new TypedSideChannel from the given keys, values, and bitSet and length.
     */
    TypedSideChannel(long[] ks, V[] vs, BitSet bs, int length) {
        keys = ks;
        values = vs;
        usedKeys = bs;
        domainSize = length;
    }
    
    
    @Override
    public ObjectSet<Long2ObjectMap.Entry<V>> long2ObjectEntrySet() {
        ObjectSet<Long2ObjectMap.Entry<V>> os = new ObjectArraySet<Long2ObjectMap.Entry<V>>(size());
        
        for(IntIterator it = new BitSetIterator(usedKeys); it.hasNext(); ) {
            int idx = it.nextInt();
            os.add(new AbstractLong2ObjectMap.BasicEntry<V>(keys[idx], values[idx]));
        }
        return os;
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
        return idx >= 0 && usedKeys.get(idx);
    }
    
    @Override
    public boolean containsValue(Object v) {
        for(IntIterator it = new BitSetIterator(usedKeys); it.hasNext(); ) {
            V val = values[it.next()];
            if(v==null && val==null) {
                return true;
            } else if (v != null && v.equals(val)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public V get(long key) {
        final int idx = findIndex(key);
        if (idx >= 0 && usedKeys.get(idx)) {
            return values[idx];
        } else {
            return defaultReturnValue();
        }
    }

    @Override
    public int size() {
        return usedKeys.cardinality();
    }
    
    @Override
    public void clear() {
        usedKeys.clear();
    }
    
    @Override
    public V put(long key, V value) {
        final int idx = findIndex(key);
        if(idx >= 0) {
            V retval = null;
            if(usedKeys.get(idx)) {
                retval = values[idx];
            }
            usedKeys.set(idx);
            values[idx] = value;
            return retval;
        } else {
            throw new IllegalArgumentException("Cannot set a key that is not in the domain.  key="
                    + key);
        }
    }
    
    @Override
    public V remove(long key) {
        final int idx = findIndex(key);
        V retval = get(key);
        if(idx >= 0) {
            usedKeys.clear(idx);
        }
        return retval;
    }
    

}
