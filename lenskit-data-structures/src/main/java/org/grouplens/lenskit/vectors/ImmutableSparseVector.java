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

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.collections.LongKeyDomain;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Immutable sparse vectors. These vectors cannot be changed, even by other
 * code, and are therefore safe to store and are thread-safe.
 *
 * <p>Use {@link #create(java.util.Map)}, {@link #empty()}, {@link #immutable()}, or
 * {@link MutableSparseVector#freeze()} to create immutable sparse vectors.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
@Immutable
public final class ImmutableSparseVector extends SparseVector implements Serializable {
    private static final long serialVersionUID = -2L;

    @SuppressFBWarnings("SE_BAD_FIELD")
    private final Map<Symbol, ImmutableSparseVector> channelVectors;
    private final Map<TypedSymbol<?>, Long2ObjectMap<?>> channels;

    private transient volatile Double norm = null;
    private transient volatile Double sum = null;
    private transient volatile Double mean = null;

    /**
     * Construct a new immutable sparse vector from a map.
     *
     * @param data The data.  It may not contain any {@code null} values.
     * @return An immutable sparse vector containing the specified data.
     */
    public static ImmutableSparseVector create(Map<Long,Double> data) {
        return MutableSparseVector.create(data).freeze();
    }

    /**
     * Create a new, empty immutable sparse vector.
     */
    ImmutableSparseVector() {
        super(LongKeyDomain.empty());
        channelVectors = Collections.emptyMap();
        channels = Collections.emptyMap();
    }

    /**
     * Create a new immutable sparse vector from a map of ratings.
     *
     * @param ratings The ratings to make a vector from. Its key set is used as
     *                the vector's key domain.
     */
    ImmutableSparseVector(Long2DoubleMap ratings) {
        super(ratings);
        channelVectors = Collections.emptyMap();
        channels = Collections.emptyMap();
    }

    /**
     * Construct a new sparse vector from a key set and a pre-existing array.  This array will copy
     * the channels passed into it, but will <emph>not</emph> copy the key set or value array.
     *
     * @param ks          The key set.  Its active keys are the key set, and all keys form the
     *                    domain.  Not copied.
     * @param vs          The value array.  Not copied.
     * @param chanVectors The channel vectors (unboxed side channels).
     * @param chans       The full map of channels.
     */
    ImmutableSparseVector(LongKeyDomain ks, double[] vs,
                          Map<Symbol, ImmutableSparseVector> chanVectors,
                          Map<TypedSymbol<?>, Long2ObjectMap<?>> chans) {
        super(ks, vs);
        channelVectors = ImmutableMap.copyOf(chanVectors);
        channels = ImmutableMap.copyOf(chans);
    }

    @Override
    boolean isMutable() {
        return false;
    }

    @Override
    public ImmutableSparseVector immutable() {
        return this;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public MutableSparseVector mutableCopy() {
        LongKeyDomain mks = keys.clone();
        double[] mvs = Arrays.copyOf(values, keys.domainSize());
        MutableSparseVector result = new MutableSparseVector(mks, mvs);
        for (Map.Entry<Symbol, ImmutableSparseVector> entry : channelVectors.entrySet()) {
            result.addVectorChannel(entry.getKey(), entry.getValue().mutableCopy());
        }
        for (Entry<TypedSymbol<?>, Long2ObjectMap<?>> entry : channels.entrySet()) {
            TypedSymbol ts = entry.getKey();
            if (!ts.getType().equals(Double.class)) {
                Long2ObjectMap<?> val = entry.getValue();
                assert val instanceof TypedSideChannel;
                result.addChannel(ts, ((TypedSideChannel) val).mutableCopy());
            } else {
                assert result.hasChannel(ts);
            }
        }
        
        return result;
    }

    @Override
    public boolean hasChannelVector(Symbol channelSymbol) {
        return channelVectors.containsKey(channelSymbol);
    }
    @Override
    public boolean hasChannel(TypedSymbol<?> channelSymbol) {
        return channels.containsKey(channelSymbol);
    }

    @Override
    public ImmutableSparseVector getChannelVector(Symbol channelSymbol) {
        return channelVectors.get(channelSymbol);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K> Long2ObjectMap<K> getChannel(TypedSymbol<K> channelSymbol) {
        return (Long2ObjectMap<K>) channels.get(channelSymbol);
    }

    @Override
    public Set<Symbol> getChannelVectorSymbols() {
        return channelVectors.keySet();
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Set<TypedSymbol<?>> getChannelSymbols() {
        return channels.keySet();
    }

    @Override
    public ImmutableSparseVector combineWith(SparseVector o) {
        LongSortedSet key = this.keyDomain();
        LongSortedSet newKey = o.keyDomain();
        MutableSparseVector result = MutableSparseVector.create(LongUtils.setUnion(key, newKey));
        result.set(this);
        result.set(o);
        return result.freeze();
    }


    // We override these three functions in the case that this vector is Immutable,
    // so we can avoid computing them more than once.
    @Override
    public double norm() {
        if (norm == null) {
            norm = super.norm();
        }
        return norm;
    }

    @Override
    public double sum() {
        if (sum == null) {
            sum = super.sum();
        }
        return sum;
    }

    @Override
    public double mean() {
        if (mean == null) {
            mean = super.mean();
        }
        return mean;
    }
}
