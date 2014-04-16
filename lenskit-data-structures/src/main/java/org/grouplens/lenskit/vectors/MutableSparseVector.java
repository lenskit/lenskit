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

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import org.grouplens.lenskit.collections.LongKeyDomain;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.collections.MoreArrays;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * Mutable version of sparse vector.
 *
 * <p>This class extends the sparse vector with support for imperative mutation
 * operations on their values.
 * <p>
 * Once created the domain of potential keys remains immutable.  Since
 * the vector is sparse, keys can be added, but only within the domain
 * the vector was constructed with.  These vectors separate the
 * concepts of the <em>key set</em>, which is the current set of active keys
 * from the <em>key domain</em>, which is the set of potential keys.  Of
 * course, the key domain must always include the key set.
 * <p>
 * Addition and subtraction are
 * supported, though they are modified from the mathematical
 * operations because they never change the set of keys.
 * Mutation operations also operate in-place to reduce the
 * reallocation and copying required.  Therefore, a common pattern is:
 *
 * <pre>
 * MutableSparseVector normalized = vector.mutableCopy();
 * normalized.subtract(normFactor);
 * </pre>
 *
 * <p>Create mutable sparse vectors using the various {@code create} static methods provided
 * ({@link #create(java.util.Collection)}, {@link #create(java.util.Collection, double)},
 * {@link #create(long...)}, {@link #create(java.util.Map)}).
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public final class MutableSparseVector extends SparseVector implements Serializable {
    private static final long serialVersionUID = 2L;

    @SuppressFBWarnings(value="SE_BAD_FIELD", justification="stored value is always serializable")
    @Nullable
    private Map<Symbol, MutableSparseVector> channelVectors;
    @SuppressFBWarnings(value="SE_BAD_FIELD", justification="stored value is always serializable")
    @Nullable
    private Map<TypedSymbol<?>, Long2ObjectMap<?>> channels;

    /**
     * Create a new empty mutable sparse vector with the specified key domain.
     *
     * @param domain The key domain.  This method is more efficient if you pass some form of {@link
     *               LongCollection}, particularly a {@link LongSortedSet}.
     * @return A mutable sparse vector with the specified domain and no active keys.
     */
    public static MutableSparseVector create(Collection<Long> domain) {
        return new MutableSparseVector(LongKeyDomain.fromCollection(domain, false));
    }

    /**
     * Create a new mutable sparse vector with the specified key domain and filled with a value.
     *
     * @param domain The key domain.  This method is more efficient if you pass some form of {@link
     *               LongCollection}, particularly a {@link LongSortedSet}.
     * @param value  The value to fill the vector with.
     * @return A mutable sparse vector with the specified domain and no active keys.
     */
    public static MutableSparseVector create(Collection<Long> domain, double value) {
        MutableSparseVector msv = create(domain);
        msv.fill(value);
        return msv;
    }

    /**
     * Create a new mutable sparse vector with the specified content.
     * @param content The content of the vector.  Pass a {@link Long2DoubleMap} for more efficiency.
     *                It may not contain any {@code null} values.
     * @return The content.
     */
    public static MutableSparseVector create(Map<Long,Double> content) {
        MutableSparseVector msv = create(content.keySet());
        msv.keys.setAllActive(true);
        final int len = msv.keys.domainSize();
        if (content instanceof Long2DoubleMap) {
            Long2DoubleMap fast = (Long2DoubleMap) content;
            for (int i = 0; i < len; i++) {
                msv.values[i] = fast.get(msv.keys.getKey(i));
            }
        } else {
            for (int i = 0; i < len; i++) {
                msv.values[i] = content.get(msv.keys.getKey(i));
            }
        }
        return msv;
    }

    /**
     * Construct a new empty vector. Since it also has an empty key domain, this
     * vector isn't very useful, because nothing can ever be put into it.
     */
    MutableSparseVector() {
        this(LongKeyDomain.empty());
    }

    /**
     * Construct a new vector with the specified domain.  The domain is used as-is, no clone is
     * taken.  The domain is cleared.
     */
    MutableSparseVector(LongKeyDomain domain) {
        super(domain);
        channelVectors = null;
        channels = null;
    }

    /**
     * Construct a new vector from the contents of a map. The key domain is the
     * key set of the map.  Therefore, no new keys can be added to this vector.
     *
     * @param keyValueMap A map providing the values for the vector.
     */
    MutableSparseVector(Long2DoubleMap keyValueMap) {
        super(keyValueMap);
        channelVectors = null;
        channels = null;
    }

    /**
     * Construct a new empty vector with the specified key domain.
     *
     * @param domain The key domain.
     */
    MutableSparseVector(Collection<Long> domain) {
        this(LongKeyDomain.fromCollection(domain, false));
    }

    /**
     * Construct a new vector with the specified keys, setting all values to a constant
     * value.  The key domain is the same as the key set, so no new
     * keys can be added to this vector.
     *
     * @param keySet The keys to include in the vector.
     * @param value  The value to assign for all keys.
     */
    MutableSparseVector(LongSet keySet, double value) {
        this(keySet);
        keys.setAllActive(true);
        DoubleArrays.fill(values, 0, keys.domainSize(), value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    MutableSparseVector(LongKeyDomain ks, double[] vs) {
        this(ks, vs, new Reference2ObjectArrayMap<Symbol, MutableSparseVector>(),
             new Reference2ObjectArrayMap<TypedSymbol<?>, Long2ObjectMap<?>>());
    }

    /**
     * Construct a new vector from existing arrays, including an
     * already instantiated Set for the used keys.
     * <p>
     * The key set and key domain are both set to the keys array.
     * The ks, vs, used, and chs objects must not be changed after
     * they are used to create this new object.
     *
     * @param ks     The key set backing the vector.
     * @param vs     The array of values backing the vector.
     * @param cvs    The initial channel vectors.
     * @param chs    The initial channel map (all channels).
     */
    MutableSparseVector(LongKeyDomain ks, double[] vs,
                        @Nullable Map<Symbol, MutableSparseVector> cvs,
                        @Nullable Map<TypedSymbol<?>, Long2ObjectMap<?>> chs) {
        super(ks, vs);
        channelVectors = cvs;
        channels = chs;
    }

    @Override
    public LongSortedSet keySet() {
        return keys.modifiableActiveSetView();
    }

    /**
     * Create a new version of this MutableSparseVector that has a
     * different domain from the current version of the vector.  All
     * elements in the current vector that are also in the new keyDomain
     * are copied over into the new vector.
     * <p>
     * Channels from the current vector are copied over to the new
     * vector, all with the changed keyDomain.
     *
     * @param keyDomain the set of keys to use for the domain of the
     *                  new vector.
     * @return the new copy of the vector.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public MutableSparseVector withDomain(LongSet keyDomain) {
        LongKeyDomain domain = LongKeyDomain.fromCollection(keyDomain, false);
        // pass an unowned domain to avoid the extra copy
        return withDomain(domain.unowned());
    }

    /**
     * Create a version of this vector with a different domain.
     * @param domain The domain (active key mask is ignored and reset).
     * @return The vector.
     */
    MutableSparseVector withDomain(LongKeyDomain domain) {
        MutableSparseVector msvNew = new MutableSparseVector(domain.clone());
        msvNew.set(this); // copy appropriate elements from "this"
        if (channelVectors != null) {
            for (Map.Entry<Symbol, MutableSparseVector> entry : channelVectors.entrySet()) {
                msvNew.addVectorChannel(entry.getKey(), entry.getValue().withDomain(domain));
            }
        }
        if (channels != null) {
            for (Entry<TypedSymbol<?>, Long2ObjectMap<?>> entry : channels.entrySet()) {
                TypedSymbol<?> key = entry.getKey();
                if (!key.getType().equals(Double.class)) {
                    Long2ObjectMap<?> chan = entry.getValue();
                    assert chan instanceof MutableTypedSideChannel;
                    msvNew.addChannel(key, ((MutableTypedSideChannel) chan).withDomain(domain));
                } else {
                    assert msvNew.hasChannel(key);
                    assert entry.getValue() instanceof MutableSparseVectorMap;
                }
            }
        }
        return msvNew;
    }

    /**
     * Create a new version of this MutableSparseVector that has keyDomain equal to this vector's
     * key set.  All elements in the current vector that are also in the new keyDomain are copied
     * over into the new vector. Channels from the current vector are copied over to the new vector,
     * all with the changed keyDomain.
     *
     * <p><b>Note:</b> the domain of this vector is not changed.
     *
     * @return the new vector with a contracted domain.
     */
    public MutableSparseVector shrinkDomain() {
        return withDomain(keys.compactCopy().unowned());
    }

    /**
     * Check if this vector is frozen.  This is mostly to have better error reporting; the values
     * array for a frozen vector is set to {@code null}, so all operations will at least fail with
     * a null pointer exception.
     */
    private void checkFrozen() {
        if (values == null) {
            throw new IllegalStateException("The mutable sparse vector is frozen");
        }
    }

    private double setAt(int index, double value) {
        assert index >= 0;
        final double v = keys.indexIsActive(index) ? values[index] : Double.NaN;
        values[index] = value;
        keys.setActive(index, true);
        return v;
    }

    /**
     * Set a value in the vector.
     *
     * @param key   The key of the value to set.
     * @param value The value to set.
     * @return The original value, or {@link Double#NaN} if the key had no value
     *         (or if the original value was {@link Double#NaN}).
     * @throws IllegalArgumentException if the key is not in the
     *                                  domain for this sparse vector.
     */
    public double set(long key, double value) {
        checkFrozen();
        final int idx = keys.getIndex(key);
        if (idx < 0) {
            throw new IllegalArgumentException("Cannot 'set' key=" + key + " that is not in the key domain.");
        }
        return setAt(idx, value);
    }

    /**
     * Set the value in the vector corresponding to a vector entry. This is
     * used in lieu of providing a {@code setValue} method on {@link VectorEntry},
     * and changes the value in constant time. The value on the entry is also changed
     * to reflect the new value.
     *
     * Is guaranteed to work on any vector that has an identical set of keys as the
     * vector from which the VectorEntry was created (such as the channels of that
     * vector), but may throw an IllegalArgumentException if the keys are not an identical
     * object even if they are the same value, to permit more efficient implementations.
     *
     * @param entry The entry to update.
     * @param value The new value.
     * @return The old value.
     * @throws IllegalArgumentException if {@code entry} does not come
     *                                  from this vector, or if the index in the entry is corrupt.
     */
    public double set(@Nonnull VectorEntry entry, double value) {
        Preconditions.checkNotNull(entry, "vector entry");
        checkFrozen();
        final SparseVector evec = entry.getVector();
        final int eind = entry.getIndex();
        if (evec == null) {
            throw new IllegalArgumentException("entry is not associated with a vector");
        } else if (!keys.isCompatibleWith(evec.keys)) {
            throw new IllegalArgumentException("entry does not have safe key domain");
        } else if (eind < 0) {
            throw new IllegalArgumentException("Cannot 'set' a key with a negative index.");
        }

        assert keys.getKey(eind) == entry.getKey();

        if (evec == this) {  // if this is the original, not a copy or channel
            entry.setValue(value);
        }
        return setAt(eind, value);
    }

    /**
     * Set the values for all items in the key domain to {@code value}.
     *
     * @param value The value to set.
     */
    public void fill(double value) {
        checkFrozen();
        DoubleArrays.fill(values, 0, keys.domainSize(), value);
        keys.setAllActive(true);
    }

    /**
     * Unset the value for a key. The key remains in the key domain, but is
     * removed from the key set.
     * @param key The key to unset.
     * @throws IllegalArgumentException if the key is not in the key domain.
     */
    public void unset(long key) {
        checkFrozen();
        final int idx = keys.getIndex(key);
        if (idx >= 0) {
            keys.setActive(idx, false);
        } else {
            throw new IllegalArgumentException("unset should only be used on keys that are in the key domain");
        }
    }

    /**
     * Unset the value for a vector entry. The key remains in the domain, but
     * is removed from the key set.
     * @param entry The entry to unset.
     */
    public void unset(@Nonnull VectorEntry entry) {
        Preconditions.checkNotNull(entry, "vector entry");
        checkFrozen();
        final SparseVector evec = entry.getVector();
        final int eind = entry.getIndex();
        if (evec == null) {
            throw new IllegalArgumentException("entry is not associated with a vector");
        } else if (!keys.isCompatibleWith(evec.keys)) {
            throw new IllegalArgumentException("entry does not have safe key domain");
        } else if (eind < 0) {
            throw new IllegalArgumentException("Cannot 'set' a key with a negative index.");
        }

        assert keys.getKey(eind) == entry.getKey();
        keys.setActive(eind, false);
    }

    /**
     * Clear all values from the set.
     */
    public void clear() {
        keys.setAllActive(false);
    }

    /**
     * Add a value to the specified entry. The key must be in the key set.
     * 
     * @param key   The key whose value should be added.
     * @param value The value to increase it by.
     * @return The new value.
     * @throws IllegalArgumentException if the key is not in the key set.
     */
    public double add(long key, double value) {
        checkFrozen();
        final int idx = keys.getIndexIfActive(key);
        if (idx >= 0) {
            values[idx] += value;
            return values[idx];
        } else {
            throw new IllegalArgumentException("invalid key " + key);
        }
    }

    /**
     * Add a value to all set keys in this array.
     *
     * @param value The value to add.
     */
    public void add(double value) {
        checkFrozen();
        // just update all values. if a value is unset, what we do to it is undefined
        final int end = keys.domainSize();
        for (int i = 0; i < end; i++) {
            values[i] += value;
        }
    }

    /**
     * Subtract another rating vector from this one.
     *
     * <p>After calling this method, every element of this vector has been
     * decreased by the corresponding element in {@var other}.  Elements
     * with no corresponding element are unchanged.
     *
     * @param other The vector to subtract.
     */
    public void subtract(final SparseVector other) {
        this.addScaled(other,-1);
    }

    /**
     * Add another rating vector to this one.
     *
     * <p>After calling this method, every element of this vector has been
     * increased by the corresponding element in {@var other}.  Elements
     * with no corresponding element are unchanged.
     *
     * @param other The vector to add.
     */
    public void add(final SparseVector other) {
        this.addScaled(other,1);
    }


    /**
     * Add a vector to this vector with a scaling factor.  It multiplies {@code v} by
     * the scaling factor {@code scale} and adds it to this vector.  Only keys set
     * in both {@code v} and {@code this} are modified.  The scaling is done
     * on-the-fly; {@code v} is unmodified.
     * @param v The vector to add to this vector.
     * @param scale The scaling factor to be applied to the vector.
     */
    public void addScaled(SparseVector v, double scale){
        checkFrozen();
        Iterator<VectorEntry> i1 = fastIterator();
        Iterator<VectorEntry> i2 = v.fastIterator();

        VectorEntry e1 = i1.hasNext() ? i1.next() : null;
        VectorEntry e2 = i2.hasNext() ? i2.next() : null;

        while (e1 != null && e2 != null) {
            final long k1 = e1.getKey();
            final long k2 = e2.getKey();
            if (k1 < k2) {
                e1 = i1.hasNext() ? i1.next() : null;
            } else if (k2 < k1) {
                e2 = i2.hasNext() ? i2.next() : null;
            } else {
                set(e1, e1.getValue() + e2.getValue()*scale);
                e1 = i1.hasNext() ? i1.next() : null;
                e2 = i2.hasNext() ? i2.next() : null;
            }
        }
    }



    /**
     * Set the values in this SparseVector to equal the values in
     * {@var other} for each key that is present in both vectors.
     *
     * <p>After calling this method, every element in this vector that has a key
     * in {@var other} has its value set to the corresponding value in
     * {@var other}. Elements with no corresponding key are unchanged, and
     * elements in {@var other} that are not in this vector are not
     * inserted.
     *
     * @param other The vector to blit its values into this vector
     */
    public void set(final SparseVector other) {
        checkFrozen();
        Iterator<VectorEntry> i1 = fastIterator(VectorEntry.State.EITHER);
        Iterator<VectorEntry> i2 = other.fastIterator();

        VectorEntry e1 = i1.hasNext() ? i1.next() : null;
        VectorEntry e2 = i2.hasNext() ? i2.next() : null;

        while (e1 != null && e2 != null) {
            final long k1 = e1.getKey();
            final long k2 = e2.getKey();
            if (k1 < k2) {
                e1 = i1.hasNext() ? i1.next() : null;
            } else if (k2 < k1) {
                e2 = i2.hasNext() ? i2.next() : null;
            } else {
                setAt(e1.getIndex(), e2.getValue());
                e1 = i1.hasNext() ? i1.next() : null;
                e2 = i2.hasNext() ? i2.next() : null;
            }
        }
    }

    /**
     * Multiply the vector by a scalar. This multiples every element in the
     * vector by {@var s}.
     *
     * @param s The scalar to rescale the vector by.
     */
    public void multiply(double s) {
        checkFrozen();
        final int end = keys.domainSize();
        for (int i = 0; i < end; i++) {
            values[i] *= s;
        }
    }

    /**
     * Multiply each element in the vector by the corresponding element in another vector.  Elements
     * not in the other vector are left unchanged.
     *
     * @param other The vector to pairwise-multiply with this one.
     */
    public void multiply(SparseVector other) {
        checkFrozen();
        Iterator<VectorEntry> i1 = fastIterator();
        Iterator<VectorEntry> i2 = other.fastIterator();

        VectorEntry e1 = i1.hasNext() ? i1.next() : null;
        VectorEntry e2 = i2.hasNext() ? i2.next() : null;

        while (e1 != null && e2 != null) {
            final long k1 = e1.getKey();
            final long k2 = e2.getKey();
            if (k1 < k2) {
                e1 = i1.hasNext() ? i1.next() : null;
            } else if (k2 < k1) {
                e2 = i2.hasNext() ? i2.next() : null;
            } else {
                set(e1, e1.getValue() * e2.getValue());
                e1 = i1.hasNext() ? i1.next() : null;
                e2 = i2.hasNext() ? i2.next() : null;
            }
        }
    }

    /**
     * Copy the rating vector.
     *
     * @return A new rating vector which is a copy of this one.
     */
    public MutableSparseVector copy() {
        return mutableCopy();
    }

    @Override
    public MutableSparseVector mutableCopy() {
        checkFrozen();
        LongKeyDomain mks = keys.clone();
        double[] mvs = java.util.Arrays.copyOf(values, keys.domainSize());

        // copy the channel maps
        Map<Symbol, MutableSparseVector> newChanVectors =
                new Reference2ObjectArrayMap<Symbol, MutableSparseVector>();
        Map<TypedSymbol<?>, Long2ObjectMap<?>> newChannels =
                new Reference2ObjectArrayMap<TypedSymbol<?>, Long2ObjectMap<?>>();
        // copy all unboxed channels into both maps
        if (channelVectors != null) {
            for (Map.Entry<Symbol, MutableSparseVector> entry : channelVectors.entrySet()) {
                Symbol key = entry.getKey();
                MutableSparseVector msv = entry.getValue().copy();
                newChanVectors.put(key, msv);
                newChannels.put(key.withType(Double.class), new MutableSparseVectorMap(msv));
            }
        }
        // copy all remaining channels into the channel map
        if (channels != null) {
            for (Entry<TypedSymbol<?>, Long2ObjectMap<?>> entry : channels.entrySet()) {
                TypedSymbol<?> key = entry.getKey();
                if (!key.getType().equals(Double.class)) {
                    Long2ObjectMap<?> chan = entry.getValue();
                    assert chan instanceof MutableTypedSideChannel;
                    newChannels.put(key, ((MutableTypedSideChannel<?>) chan).mutableCopy());
                } else {
                    assert newChannels.containsKey(key);
                    assert entry.getValue() instanceof MutableSparseVectorMap;
                }
            }
        }

        return new MutableSparseVector(mks, mvs, newChanVectors, newChannels);
    }

    /**
     * Accumulate all keys used by this vector and its side channels into a single
     * key domain.
     * @param domain The domain to modify. It must be compatible with this vector's
     *               key domain, and all keys used by this vector or any of its side
     *               channels will be activated in the key domain.
     */
    private void accumulateAllKeys(LongKeyDomain domain) {
        assert keys.isCompatibleWith(domain);
        domain.activate(keys.getActiveMask());
        if (channels != null) {
            for (Entry<TypedSymbol<?>, Long2ObjectMap<?>> entry: channels.entrySet()) {
                Long2ObjectMap<?> map = entry.getValue();
                if (map instanceof MutableTypedSideChannel) {
                    domain.activate(((MutableTypedSideChannel) map).keys.getActiveMask());
                } else if (map instanceof MutableSparseVectorMap) {
                    ((MutableSparseVectorMap) map).msv.accumulateAllKeys(domain);
                } else {
                    throw new AssertionError("unexpected channel type " + map.getClass());
                }
            }
        }
    }

    @Override
    public ImmutableSparseVector immutable() {
        checkFrozen();
        // TODO Don't copy bit set if we are freezing?
        LongKeyDomain newDomain = keys.clone();
        accumulateAllKeys(newDomain);
        return immutable(false, newDomain.compactCopy().unowned());
    }

    /**
     * Construct an immutable sparse vector from this vector's data (if possible),
     * invalidating this vector in the process. Any subsequent use of this
     * vector is invalid; all access must be through the returned immutable
     * vector. The frozen vector's key domain is equal to this vector's key set.
     *
     * @return An immutable vector built from this vector's data.
     * @see #immutable()
     */
    public ImmutableSparseVector freeze() {
        return immutable(true, keys.compactCopy().unowned());
    }

    /**
     * Construct an immutable sparse vector from this vector's data.
     *
     * {@var freeze} indicates whether this (mutable) vector should be
     * frozen as a side effect of generating the immutable form of the
     * vector.  If it is okay to freeze this mutable vector, then
     * parts of the mutable vector may be used to efficiently form the
     * new immutable vector.  Otherwise, the parts of the mutable
     * vector must be copied, to ensure immutability.
     * <p>
     * {@var freeze} applies
     * also to the channels: any channels of this mutable vector may
     * also be frozen if the vector is frozen, to avoid copying them.
     * <p>
     * {@var keyDomain} is the key domain for the new immutable sparse
     * vector, which should be the key set of the original vector.
     *
     * @param freeze Whether to freeze the vector.
     * @param keyDomain The key set to use as the domain.
     * @return An immutable vector built from this vector's data.
     */
    private ImmutableSparseVector immutable(boolean freeze, LongKeyDomain keyDomain) {
        double[] nvs;
        LongKeyDomain newDomain = keyDomain.clone();
        if (newDomain.isCompatibleWith(keys)) {
            nvs = (freeze && values.length == newDomain.size())
                    ? values
                    : java.util.Arrays.copyOf(values, newDomain.domainSize());
            newDomain.setActive(keys.getActiveMask());
        } else {
            nvs = new double[newDomain.domainSize()];

            int i = 0;
            int j = 0;
            final int end = keys.domainSize();
            while (i < nvs.length && j < end) {
                final long ki = newDomain.getKey(i);
                final long kj = keys.getKey(j);
                if (ki == kj) {
                    nvs[i] = values[j];
                    newDomain.setActive(i, keys.indexIsActive(j));
                    i++;
                    j++;
                } else if (kj < ki) {
                    j++;
                } else {
                    // untestable
                    throw new AssertionError("new domain not subset of old domain");
                }
            }
        }

        Map<Symbol, ImmutableSparseVector> newChannelVectors = new Reference2ObjectArrayMap<Symbol, ImmutableSparseVector>();
        Map<TypedSymbol<?>, Long2ObjectMap<?>> newChannels = new Reference2ObjectArrayMap<TypedSymbol<?>, Long2ObjectMap<?>>();
        // We recursively generate immutable versions of all channels.  If freeze
        // is true, these versions will be made without copying.
        if (channelVectors != null) {
            for (Map.Entry<Symbol, MutableSparseVector> entry : channelVectors.entrySet()) {
                Symbol key = entry.getKey();
                ImmutableSparseVector chan = entry.getValue().immutable(freeze, newDomain);
                newChannelVectors.put(key, chan);
                newChannels.put(key.withType(Double.class), new SparseVectorMap(chan));
            }
        }

        if (channels != null) {
            for (Entry<TypedSymbol<?>, Long2ObjectMap<?>> entry : channels.entrySet()) {
                TypedSymbol<?> key = entry.getKey();
                if (!key.getType().equals(Double.class)) {
                    Long2ObjectMap<?> chan = entry.getValue();
                    assert chan instanceof MutableTypedSideChannel;
                    newChannels.put(key, ((MutableTypedSideChannel<?>) chan).immutable(newDomain, freeze));
                } else {
                    assert newChannels.containsKey(key);
                    assert entry.getValue() instanceof MutableSparseVectorMap;
                }
            }
        }
        
        ImmutableSparseVector isv = new ImmutableSparseVector(newDomain, nvs,
                                                              newChannelVectors,
                                                              newChannels);
        if (freeze) {
            values = null;
        }
        return isv;
    }

    /**
     * Wrap key and value arrays in a sparse vector.
     *
     * <p>This method allows a new vector to be constructed from
     * pre-created arrays.  After wrapping arrays in a sparse vector, client
     * code should not modify them (particularly the {@var keys}
     * array).  The key domain of the newly created vector will be the
     * same as the keys.
     *
     * @param keys   Array of entry keys. This array must be in sorted order and
     *               be duplicate-free.
     * @param values The values for the vector, in key order.
     * @return A sparse vector backed by the provided arrays.
     * @throws IllegalArgumentException if there is a problem with the provided
     *                                  arrays (length mismatch, {@var keys} not sorted, etc.).
     */
    public static MutableSparseVector wrap(long[] keys, double[] values) {
        return wrap(keys, values, keys.length);
    }

    /**
     * Wrap key and value arrays in a sparse vector.
     *
     * <p> This method allows a new vector to be constructed from
     * pre-created arrays. After wrapping arrays in a sparse vector,
     * client code should not modify them (particularly the {@var
     * keys} array).  The key domain of the newly created vector will
     * be the same as the keys.
     *
     * @param keys   Array of entry keys. This array must be in sorted order and
     *               be duplicate-free.
     * @param values The values for the vector.
     * @param size   The size of the vector; only the first {@var size}
     *               entries from each array are actually used.
     * @return A sparse vector backed by the provided arrays.
     * @throws IllegalArgumentException if there is a problem with the provided
     *                                  arrays (length mismatch, {@var keys} not sorted, etc.).
     */
    public static MutableSparseVector wrap(long[] keys, double[] values, int size) {
        if (values.length < size) {
            throw new IllegalArgumentException("value array too short");
        }
        if (keys.length < size) {
            throw new IllegalArgumentException("key array too short");
        }
        if (!MoreArrays.isSorted(keys, 0, size)) {
            throw new IllegalArgumentException("item array not sorted");
        }
        LongKeyDomain keySet = LongKeyDomain.wrap(keys, size, true);
        return new MutableSparseVector(keySet, values);
    }

    /**
     * Wrap key and value array lists in a mutable sparse vector. Don't modify
     * the original lists once this has been called!  There must be at least
     * as many values as keys.  The value list will be truncated to the length
     * of the key list.
     *
     * @param keyList   The list of keys
     * @param valueList The list of values
     * @return A backed by the backing stores of the provided lists.
     */
    public static MutableSparseVector wrap(LongArrayList keyList, DoubleArrayList valueList) {
        long[] keys = keyList.elements();
        double[] values = valueList.elements();
        return MutableSparseVector.wrap(keys, values, keyList.size());
    }

    /**
     * Create a mutable sparse vector with a fixed set of keys.  This is mostly useful for tests.
     *
     * @param keys The key domain.
     * @return A mutable sparse vector with the specified keys in its domain, all unset.
     */
    public static MutableSparseVector create(long... keys) {
        return MutableSparseVector.create(new LongOpenHashSet(keys));
    }

    /**
     * Create a new {@code MutableSparseVector} from unsorted key and value
     * arrays. The provided arrays will be modified and should not be used
     * by the client after this operation has completed. The key domain of
     * the new {@code MutableSparseVector} will be the same as {@code keys}.
     *
     * @param keys Array of entry keys. This should be duplicate-free.
     * @param values The values of the vector, in key order.
     * @return A sparse vector backed by the provided arrays.
     * @throws IllegalArgumentException if there is a problem with the provided
     *                                  arrays (length mismatch, etc.).
     */
    public static MutableSparseVector wrapUnsorted(long[] keys, double[] values) {
        IdComparator comparator = new IdComparator(keys);
        ParallelSwapper swapper = new ParallelSwapper(keys, values);
        Arrays.quickSort(0, keys.length, comparator, swapper);

        return MutableSparseVector.wrap(keys, values);
    }

    /**
     * Remove the channel stored under a particular symbol.
     *
     * @param channelSymbol the symbol under which the channel was
     *                      stored in the vector.
     * @return the channel, which is itself a sparse vector.
     * @throws IllegalArgumentException if this vector does not have
     *                                  such a channel at this time.
     */
    public SparseVector removeChannelVector(Symbol channelSymbol) {
        checkFrozen();
        SparseVector retval;
        if (hasChannelVector(channelSymbol)) {
            assert channelVectors != null;
            assert channels != null;
            retval = channelVectors.remove(channelSymbol);
            channels.remove(channelSymbol.withType(Double.class));
            return retval;
        }
        throw new IllegalArgumentException("No such channel " +
                                           channelSymbol.getName());
    }

    /**
     * Remove the typed channel stored under a particular symbol.
     *
     * @param channelSymbol the symbol under which the channel was
     *                      stored in the vector.
     * @return the channel
     * @throws IllegalArgumentException if this vector does not have
     *                                  such a channel at this time.
     */
    @SuppressWarnings("unchecked")
    public <K> Long2ObjectMap<K> removeChannel(TypedSymbol<K> channelSymbol) {
        checkFrozen();
        Long2ObjectMap<K> retval;
        if (hasChannel(channelSymbol)) {
            assert channels != null;
            retval = (Long2ObjectMap<K>) channels.remove(channelSymbol);
            if (channelSymbol.getType().equals(Double.class)) {
                assert channelVectors != null;
                channelVectors.remove(channelSymbol.getRawSymbol());
            }
            return retval;
        }
        throw new IllegalArgumentException("No such channel " +
                                           channelSymbol.getName() +
                                           " with type " +
                                           channelSymbol.getType().getSimpleName());
    }

    /**
     * Remove all channels stored in this vector.
     */
    public void removeAllChannels() {
        checkFrozen();
        channelVectors = null;
        channels = null;
    }

    /**
     * Add a channel to this vector.  The new channel will be empty,
     * and will have the same key domain as this vector.
     *
     * @param channelSymbol the symbol under which this new channel
     *                      should be created.
     * @return the newly created channel
     * @throws IllegalArgumentException if there is already a channel
     *                                  with that symbol
     */
    public MutableSparseVector addChannelVector(Symbol channelSymbol) {
        checkFrozen();
        if (hasChannelVector(channelSymbol)) {
            throw new IllegalArgumentException("Channel " + channelSymbol.getName()
                                               + " already exists");
        }
        MutableSparseVector theChannel = new MutableSparseVector(keys.inactiveCopy());
        addVectorChannel(channelSymbol,  theChannel);
        return theChannel;
    }

    /**
     * Add a typed channel to this vector.  The new channel will be empty,
     * and will have the same key domain as this vector.
     *
     * @param channelSymbol the symbol under which this new channel
     *                      should be created.
     * @return the newly created channel
     * @throws IllegalArgumentException if there is already a channel
     *                                  with that symbol
     */
    @SuppressWarnings("unchecked")
    public <K> Long2ObjectMap<K> addChannel(TypedSymbol<K> channelSymbol) {
        checkFrozen();
        if (hasChannel(channelSymbol)) {
            throw new IllegalArgumentException("Channel " + channelSymbol.getName()
                                               + " with type " + channelSymbol.getType().getSimpleName() 
                                               + " already exists");
        }
        if (channelSymbol.getType().equals(Double.class)) {
            addChannelVector(channelSymbol.getRawSymbol());
            assert channels != null;
            return (Long2ObjectMap<K>) channels.get(channelSymbol);
        } else {
            MutableTypedSideChannel<K> theChannel = new MutableTypedSideChannel<K>(keys.inactiveCopy());
            addChannel(channelSymbol, theChannel);
            return theChannel;
        }
    }

    /**
     * Add a channel to the vector, even if there is already a
     * channel with the same symbol.  If there already was such a channel
     * it will be unchanged; otherwise a new empty channel will be created
     * with the same key domain as this vector.
     *
     * @param channelSymbol the symbol under which this new channel
     *                      should be created.
     * @return the newly created channel
     */
    public MutableSparseVector getOrAddChannelVector(Symbol channelSymbol) {
        MutableSparseVector chan = getChannelVector(channelSymbol);
        if (chan == null) {
            chan = addChannelVector(channelSymbol);
        }
        return chan;
    }

    /**
     * Add a typed channel to the vector, even if there is already a
     * channel with the same symbol.  The new channel will be empty,
     * and will have the same key domain as this vector.
     *
     * @param channelSymbol the symbol under which this new channel
     *                      should be created.
     * @return the newly created channel
     */
    @SuppressWarnings("unchecked")
    public <K> Long2ObjectMap<K> getOrAddChannel(TypedSymbol<K> channelSymbol) {
        Long2ObjectMap<K> chan = getChannel(channelSymbol);
        if (chan == null) {
            chan = addChannel(channelSymbol);
        }
        return chan;
    }

    void addVectorChannel(Symbol key, MutableSparseVector vectorEntries) {
        Preconditions.checkArgument(keys.isCompatibleWith(vectorEntries.keys),
                                    "vector has incompatible key domain");
        if (channelVectors == null) {
            channelVectors = new Reference2ObjectArrayMap<Symbol, MutableSparseVector>();
            if (channels == null) {
                channels = new Reference2ObjectArrayMap<TypedSymbol<?>, Long2ObjectMap<?>>();
            }
        }
        channelVectors.put(key, vectorEntries);
        assert channels != null;
        channels.put(key.withType(Double.class), new MutableSparseVectorMap(vectorEntries));
    }

    <T> void addChannel(TypedSymbol<T> sym, MutableTypedSideChannel<T> chan) {
        Preconditions.checkArgument(keys.isCompatibleWith(chan.keys),
                                    "vector has incompatible key domain");
        Preconditions.checkArgument(!sym.getType().equals(Double.class),
                                    "cannot add double channel like this");
        if (channels == null) {
            channels = new Reference2ObjectArrayMap<TypedSymbol<?>, Long2ObjectMap<?>>();
        }
        channels.put(sym, chan);
    }

    @Override
    public boolean hasChannelVector(Symbol channelSymbol) {
        return channelVectors != null && channelVectors.containsKey(channelSymbol);
    }

    @Override
    public boolean hasChannel(TypedSymbol<?> channelSymbol) {
        return channels != null && channels.containsKey(channelSymbol);
    }

    @Override
    public MutableSparseVector getChannelVector(Symbol channelSymbol) {
        checkFrozen();
        return channelVectors == null ? null : channelVectors.get(channelSymbol);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K> Long2ObjectMap<K> getChannel(TypedSymbol<K> channelSymbol) {
        checkFrozen();
        return channels == null ? null : (Long2ObjectMap<K>) channels.get(channelSymbol);
    }

    @Override
    public Set<Symbol> getChannelVectorSymbols() {
        if (channelVectors == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(channelVectors.keySet());
        }
    }

    @Override
    public Set<TypedSymbol<?>> getChannelSymbols() {
        if (channels == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(channels.keySet());
        }
    }

    @Override
    public MutableSparseVector combineWith(SparseVector o) {
        LongSortedSet key = this.keyDomain();
        LongSortedSet newKey = o.keyDomain();
        MutableSparseVector result = MutableSparseVector.create(LongUtils.setUnion(key, newKey));
        result.set(this);
        result.set(o);
        return result;
    }

    private static class IdComparator extends AbstractIntComparator {
        private long[] keys;

        @SuppressWarnings("PMD.ArrayIsStoredDirectly")
        public IdComparator(long[] keys) {
            this.keys = keys;
        }

        @Override
        public int compare(int i, int i2) {
            return LongComparators.NATURAL_COMPARATOR.compare(keys[i], keys[i2]);
        }
    }

    private static class ParallelSwapper implements Swapper {

        private long[] keys;
        private double[] values;

        @SuppressWarnings("PMD.ArrayIsStoredDirectly")
        public ParallelSwapper(long[] keys, double[] values) {
            this.keys = keys;
            this.values = values;
        }

        @Override
        public void swap(int i, int i2) {
            long lTemp = keys[i];
            keys[i] = keys[i2];
            keys[i2] = lTemp;

            double dTemp = values[i];
            values[i] = values[i2];
            values[i2] = dTemp;
        }
    }
}
