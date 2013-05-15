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

import java.util.BitSet;

class ImmutableTypedSideChannel<V> extends TypedSideChannel<V> {
    private static final long serialVersionUID = 1L;

    /**
     * Build a new TypedSideChannel from the given keys, it is assumed that the key array is sorted, 
     * duplicate free. The key array will be the key domain.
     * @param ks The array of keys backing this vector. They must be sorted.
     */
    ImmutableTypedSideChannel(long[] ks) {
        super(ks);
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
    ImmutableTypedSideChannel(long[] ks, V[] vs) {
        super(ks, vs);
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
    ImmutableTypedSideChannel(long[] ks, V[] vs, int length) {
        super(ks, vs, length);
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
    ImmutableTypedSideChannel(long[] keys, int length) {
        super(keys, length);
    }
    
    /**
     * Build a new TypedSideChannel from the given keys, values, and bitSit.
     * It is assumed that the keys and values array are the same length as the bitSet, 
     * that the keys are sorted and duplicate free.
     * The keys array will become the key set.
     */
    ImmutableTypedSideChannel(long[] ks, V[] vs, BitSet bs) {
        super(ks, vs, bs);
    }
    
    /**
     * Build a new TypedSideChannel from the given keys, values, and bitSet and length.
     */
    ImmutableTypedSideChannel(long[] ks, V[] vs, BitSet bs, int length) {
        super(ks, vs, bs, length);
    }
        @Override
    public void clear() {
        throw new UnsupportedOperationException("ImmutableTypedSideChannels cannot be mutated");
    }
    
    @Override
    public V put(long key, V value) {
        throw new UnsupportedOperationException("ImmutableTypedSideChannels cannot be mutated");
    }
    
    @Override
    public void defaultReturnValue(V rv) {
        throw new UnsupportedOperationException("ImmutableTypedSideChannels cannot be mutated");
    }
    
    @Override
    public V remove(long key) {
        throw new UnsupportedOperationException("ImmutableTypedSideChannels cannot be mutated");
    }

}
