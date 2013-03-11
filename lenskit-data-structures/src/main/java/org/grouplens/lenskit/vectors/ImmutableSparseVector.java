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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import org.grouplens.lenskit.symbols.Symbol;

/**
 * Immutable sparse vectors. These vectors cannot be changed, even by other
 * code, and are therefore safe to store and are thread-safe.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @compat Public
 */
@Immutable
public final class ImmutableSparseVector extends SparseVector implements Serializable {
    private static final long serialVersionUID = -2L;

    private final Map<Symbol, ImmutableSparseVector> channelMap;

    /**
     * Create a new, empty immutable sparse vector.
     */
    public ImmutableSparseVector() {
        this(new long[0], new double[0]);
    }

    /**
     * Create a new immutable sparse vector from a map of ratings.
     *
     * @param ratings The ratings to make a vector from. Its key set is used as
     *                the vector's key domain.
     */
    public ImmutableSparseVector(Long2DoubleMap ratings) {
        super(ratings);
        channelMap = new Reference2ObjectArrayMap<Symbol, ImmutableSparseVector>();
    }

    /**
     * Construct a new vector from existing arrays.  It is assumed that the keys
     * are sorted and duplicate-free, and that the values is the same length. The
     * key array is the key domain, and all keys are considered used.
     * No new keys can be added to this vector.  Clients should call
     * the wrap() method rather than directly calling this constructor.
     *
     * @param ks The array of keys backing this vector. They must be sorted.
     * @param vs The array of values backing this vector.
     */
    protected ImmutableSparseVector(long[] ks, double[] vs) {
        this(ks, vs, ks.length);
    }

    /**
     * Construct a new sparse vector from pre-existing arrays. These arrays must
     * be sorted in key order and cannot contain duplicate keys; this condition
     * is not checked.
     *
     * @param ks The key array (will be the key domain).
     * @param vs The value array.
     * @param sz The length to actually use.
     */
    protected ImmutableSparseVector(long[] ks, double[] vs, int sz) {
        super(ks, vs, sz);
        channelMap = new Reference2ObjectArrayMap<Symbol, ImmutableSparseVector>();
    }

    /**
     * Construct a new sparse vector from pre-existing arrays. These arrays must
     * be sorted in key order and cannot contain duplicate keys; this condition
     * is not checked.  The new vector will have a copy of the
     * channels that are passed into it.
     *
     * @param ks       the key array (will be the key domain).
     * @param vs       the value array.
     * @param sz       the length to actually use.
     * @param used     the keys that actually have values currently.
     * @param channels The side channel values.
     */
    protected ImmutableSparseVector(long[] ks, double[] vs, int sz, BitSet used,
                                    Map<Symbol, ImmutableSparseVector> channels) {
        super(ks, vs, sz, used);
        channelMap = channels;
    }

    @Override
    public ImmutableSparseVector immutable() {
        return this;
    }

    @Override
    public MutableSparseVector mutableCopy() {
        MutableSparseVector result = new MutableSparseVector(keys, Arrays.copyOf(values, domainSize),
                                                             domainSize, (BitSet) usedKeys.clone());
        for (Map.Entry<Symbol, ImmutableSparseVector> entry : channelMap.entrySet()) {
            result.addChannel(entry.getKey(), entry.getValue().mutableCopy());
        }
        return result;
    }

    @Override
    public boolean hasChannel(Symbol channelSymbol) {
        return channelMap.containsKey(channelSymbol);
    }

    @Override
    public ImmutableSparseVector channel(Symbol channelSymbol) {
        if (hasChannel(channelSymbol)) {
            return channelMap.get(channelSymbol);
        }
        throw new IllegalArgumentException("No existing channel under name " +
                                                   channelSymbol.getName());
    }

}
