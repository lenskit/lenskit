/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

import org.grouplens.lenskit.collections.BitSetIterator;
import org.grouplens.lenskit.collections.IntIntervalList;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.collections.MoreArrays;
import org.grouplens.lenskit.symbols.Symbol;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Mutable version of sparse vector.
 *
 * <p>This extends the sparse vector with support for imperative mutation
 * operations on their values.  
 * Once created the domain of potential keys remains immutable.  Since
 * the vector is sparse, keys can be added, but only within the domain
 * the vector was constructed with.  These vectors separate the
 * concepts of the *key set*, which is the current set of active keys
 * from the *key domain*, which is the set of potential keys.  Of
 * course, the key domain must always include the key set.
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
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @compat Public
 */
public final class MutableSparseVector extends SparseVector implements Serializable {
    private static final long serialVersionUID = 1L;

    // It is possible for objects of this type to be converted to an
    // ImmutableSparseVector.  For efficiency, rather than copy the
    // data, we can "freeze" this implementation so it can no longer
    // be changed.  Setting this variable to be false causes all
    // mutation methods to throw an exception if they are called.
    protected boolean isMutable = true;   

    private Map<Symbol, MutableSparseVector> channelMap;

    /**
     * Construct a new empty vector. Since it also has an empty key domain, this
     * vector isn't very useful, because nothing can ever be put into it.
     */
    public MutableSparseVector() {
        this(new long[0], new double[0]);
    }

    /**
     * Construct a new vector from the contents of a map. The key domain is the
     * key set of the map.  Therefore, no new keys can be added to this vector.
     *
     * @param ratings A map providing the values for the vector.
     */
    public MutableSparseVector(Long2DoubleMap ratings) {
	super(ratings);
	channelMap = new Reference2ObjectArrayMap<Symbol, MutableSparseVector>();
    }

    /**
     * Construct a new empty vector with the specified key domain.
     *
     * @param domain The key domain.
     */
    public MutableSparseVector(Collection<Long> domain) {
	super(domain);
	channelMap = new Reference2ObjectArrayMap<Symbol, MutableSparseVector>();
    }

    /**
     * Construct a new vector with the specified keys, setting all values to a constant
     * value.  The key domain is the same as the key set, so no new
     * keys can be added to this vector.
     *
     * @param keySet The keys to include in the vector.
     * @param value  The value to assign for all keys.
     */
    public MutableSparseVector(LongSet keySet, double value) {
        this(keySet);
        DoubleArrays.fill(values, 0, domainSize, value);
        usedKeys.set(0, domainSize);
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
    protected MutableSparseVector(long[] ks, double[] vs) {
        this(ks, vs, ks.length);
    }

    /**
     * Construct a new vector from existing arrays. It is assumed that
     * the keys are sorted and duplicate-free, and that the keys and
     * values both have at least {@var length} items.  The key set
     * and key domain are both set to the keys array.  Clients should
     * call the wrap() method rather than directly calling this
     * constructor.
     *
     * @param ks     The array of keys backing the vector. It must be sorted.
     * @param vs     The array of values backing the vector.
     * @param length Number of items to actually use.
     */
    protected MutableSparseVector(long[] ks, double[] vs, int length) {
	super(ks, vs, length);
	channelMap = new Reference2ObjectArrayMap<Symbol, MutableSparseVector>();
    }

    /**
     * Construct a new vector from existing arrays, including an
     * already instantiated Set for the used keys. It is assumed that the keys
     * are sorted and duplicate-free, and that the keys and values
     * both have at least {@param length} items.
     * The key set and key domain are both set to the keys array.
     *
     * @param ks     The array of keys backing the vector.
     * @param vs     The array of values backing the vector.
     * @param length Number of items to actually use.
     * @param used   The entries in use.
     */
    protected MutableSparseVector(long[] ks, double[] vs, int length, BitSet used) {
	super(ks, vs, length, used);
	channelMap = new Reference2ObjectArrayMap<Symbol, MutableSparseVector>();
    }

    /**
     * Construct a new vector from existing arrays, including an
     * already instantiated Set for the used keys. It is assumed that the keys
     * are sorted and duplicate-free, and that the keys and values
     * both have at least {@param length} items.
     * The key set and key domain are both set to the keys array.
     * The ks, vs, used, and chs objects must not be changed after
     * they are used to create this new object.
     *
     * @param ks     The array of keys backing the vector.
     * @param vs     The array of values backing the vector.
     * @param length Number of items to actually use.
     * @param used   The entries in use.
     */
    protected MutableSparseVector(long[] ks, double[] vs, int length, BitSet used,
				  Map<Symbol, MutableSparseVector> chs) {
	super(ks, vs, length, used);
	channelMap = chs;
    }

    /**
     * Create a new version of this MutableSparseVector that has a
     * different domain from the current version of the vector.  All
     * elements in the current vector that are also in the new keyDomain
     * are copied over into the new vector.  
     * Channels from the current vector are copied over to the new
     * vector, all with the changed keyDomain.
     * 
     * @param keyDomain the set of keys to use for the domain of the
     * new vector.
     * @returns the new copy of the vector.
     */
    public MutableSparseVector withDomain(LongSet keyDomain) {
	MutableSparseVector msvNew = new MutableSparseVector(keyDomain);
	msvNew.set(this); // copy appropriate elements from "this"
	for (Map.Entry<Symbol, MutableSparseVector> entry : channelMap.entrySet()) {
	    msvNew.addChannel(entry.getKey(), entry.getValue().withDomain(keyDomain));
	}
	return msvNew;
    }

    /**
     * Create a new version of this MutableSparseVector that has
     * keyDomain equal to the current set of items that are set in
     * "this" vector.  All elements in the current vector that are
     * also in the new keyDomain are copied over into the new vector.
     * Channels from the current vector are copied over to the new
     * vector, all with the changed keyDomain.
     * 
     * @returns the new copy of the vector.
     */

    public MutableSparseVector withDomain() {
	LongSet newDomain = new LongArraySet();
	for (VectorEntry entry : this) {
	    newDomain.add(entry.getKey());
	}
	return this.withDomain(newDomain);
    }

    /**
     * Check if this vector is Mutable.
     */
    private void checkMutable() {
        if (! isMutable) {
            throw new IllegalStateException("Vector is frozen");
        }
    }

    private double setAt(int idx, double value) {
        if (idx >= 0) {
            final double v = usedKeys.get(idx) ? values[idx] : Double.NaN;
            values[idx] = value;
            clearCachedValues();
            usedKeys.set(idx);
            return v;
        } else {
	    throw new IllegalArgumentException("Cannot set the value on a negative index");
        }
    }

    /**
     * Set a value in the vector.
     *
     * @param key   The key of the value to set.
     * @param value The value to set.
     * @return The original value, or {@link Double#NaN} if the key had no value
     *         (or if the original value was {@link Double#NaN}).
     * @throws IllegalArgumentException if the key is not in the
     * domain for this sparse vector.
     */
    public double set(long key, double value) {
        checkMutable();
        final int idx = findIndex(key);
	if (idx < 0) throw new IllegalArgumentException("Cannot set a key that is not in the domain.  key="
							+ key);
        return setAt(idx, value);
    }

    /**
     * Set the value in the vector corresponding to a vector entry. This is
     * used in lieu of providing a {@code setValue} method on {@link VectorEntry},
     * and changes the value in constant time. The value on the entry is also changed
     * to reflect the new value.
     *
     * @param entry The entry to update.
     * @param value The new value.
     * @return The old value.
     * @throws IllegalArgumentException if {@code entry} does not come
     * from this vector, or if the index in the entry is corrupt.
     */
    public double set(VectorEntry entry, double value) {
        if (entry.getVector() != this) {
            throw new IllegalArgumentException("entry not from correct vector");
        }
        final int idx = entry.getIndex();
        entry.setValue(value);
        return setAt(idx, value);
    }

    /**
     * Set the values for all items in the key domain to {@code value}.
     *
     * @param value The value to set.
     */
    public void fill(double value) {
        DoubleArrays.fill(values, 0, domainSize, value);
        usedKeys.set(0, domainSize);
    }

    /**
     * Clear the value for a key.  The key remains in the key domain, but is
     * removed from the key set.
     *
     * @param key The key to clear.
     */
    public void clear(long key) {
        final int idx = findIndex(key);
        if (idx >= 0) {
            usedKeys.clear(idx);
        }
    }

    /**
     * Clear the value for a vector entry.
     *
     * @param e The entry to clear.
     * @see #clear(long)
     */
    public void clear(VectorEntry e) {
        if (e.getVector() != this) {
            throw new IllegalArgumentException("clearing vector from wrong entry");
        }
        usedKeys.clear(e.getIndex());
    }

    /**
     * Clear all values from the set.
     */
    public void clear() {
        usedKeys.clear();
    }

    /**
     * Add a value to the specified entry. The value must be in the key set.
     *
     * @param key   The key whose value should be added.
     * @param value The value to increase it by.
     * @return The new value (or {@link Double#NaN} if no such key existed).
     */
    public double add(long key, double value) {
        checkMutable();
        final int idx = findIndex(key);
        if (idx >= 0 && usedKeys.get(idx)) {
            clearCachedValues();
            values[idx] += value;
            return values[idx];
        } else {
            return Double.NaN;
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
        checkMutable();
        clearCachedValues();
        int i = 0;
        for (VectorEntry oe : other.fast()) {
            final long k = oe.getKey();
            while (i < domainSize && keys[i] < k) {
                i++;
            }
            if (i >= domainSize) {
                break; // no more entries
            }
            if (keys[i] == k && usedKeys.get(i)) {
                values[i] -= oe.getValue();
            } // otherwise, key is greater; advance outer
        }
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
        checkMutable();
        clearCachedValues();
        int i = 0;
        for (VectorEntry oe : other.fast()) {
            final long k = oe.getKey();
            while (i < domainSize && keys[i] < k) {
                i++;
            }
            if (i >= domainSize) {
                break; // no more entries
            }
            if (keys[i] == k && usedKeys.get(i)) {
                values[i] += oe.getValue();
            } // otherwise, key is greater; advance outer 
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
        checkMutable();
        clearCachedValues();
        int i = 0;
        for (VectorEntry oe : other.fast()) {
            final long k = oe.getKey();
            while (i < domainSize && keys[i] < k) {
                i++;
            }
            if (i >= domainSize) {
                break; // no more entries
            }
            if (keys[i] == k) {
                values[i] = oe.getValue();
                usedKeys.set(i);
            } // otherwise, key is greater; advance outer 
        }
    }

    /**
     * Multiply the vector by a scalar. This multiples every element in the
     * vector by {@var s}.
     *
     * @param s The scalar to rescale the vector by.
     */
    public void scale(double s) {
        clearCachedValues();
        BitSetIterator iter = new BitSetIterator(usedKeys, 0, domainSize);
        while (iter.hasNext()) {
            int i = iter.nextInt();
            values[i] *= s;
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

    private Map<Symbol, MutableSparseVector> copyOfChannelMap() {
	Map<Symbol, MutableSparseVector> copyOfChannels =
	    new Reference2ObjectArrayMap<Symbol, MutableSparseVector>();
	for (Map.Entry<Symbol, MutableSparseVector> entry : channelMap.entrySet()) {
	    copyOfChannels.put(entry.getKey(), entry.getValue().copy());
	}
	return copyOfChannels;
    }

    @Override
    public MutableSparseVector mutableCopy() {
        double[] nvs = Arrays.copyOf(values, domainSize);
        BitSet nbs = (BitSet) usedKeys.clone();

        return new MutableSparseVector(keys, nvs, domainSize, nbs, copyOfChannelMap());
    }

    @Override
    public ImmutableSparseVector immutable() {
        return immutable(false);
    }

    // Mark a mutable sparse vector as immutable, so it can be safely
    // returned in contexts in which it must not be changed by the
    // client.  Currently used to return channels from a mutable
    // vector that has been itself frozen.
    private MutableSparseVector partialFreeze() {
	isMutable = false;
	return this;
    }

    /**
     * Construct an immutable sparse vector from this vector's data,
     * invalidating this vector in the process. Any subsequent use of this
     * vector is invalid; all access must be through the returned immutable
     * vector. The frozen vector's key set is equal to this vector's key domain.
     *
     * @return An immutable vector built from this vector's data.
     */
    public ImmutableSparseVector freeze() {
        return immutable(true);
    }

    /**
     * Construct an immutable sparse vector from this vector's data.
     *
     * @param freeze indicates whether this (mutable) vector should be
     * frozen as a side effect of generating the immutable form of the
     * vector.  If it is okay to freeze this mutable vector, then
     * parts of the mutable vector may be used to efficiently form the
     * new immutable vector.  Otherwise, the parts of the mutable
     * vector must be copied, to ensure immutability.  freeze applies
     * also to the channels: any channels of this mutable vector may
     * also be frozen if the vector is frozen, to avoid copying them.
     * @return An immutable vector built from this vector's data.
     */
    private ImmutableSparseVector immutable(boolean freeze) {
        ImmutableSparseVector isv;
	double[] nvs = freeze ? values : Arrays.copyOf(values, domainSize);
	Map<Symbol, ImmutableSparseVector> newChannelMap =
	    new Reference2ObjectArrayMap<Symbol, ImmutableSparseVector>();
	// We freeze all entries in the channep map, so we don't have
	// to make copies of them.
	for (Map.Entry<Symbol, MutableSparseVector> entry : channelMap.entrySet()) {
	    if (freeze) {
		newChannelMap.put(entry.getKey(), entry.getValue().freeze());
	    } else {
		newChannelMap.put(entry.getKey(), entry.getValue().copy().freeze());
	    }		
	}
	isv = new ImmutableSparseVector(keys, nvs, domainSize, usedKeys, newChannelMap);

        if (freeze) {
            isMutable = false;
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
        if (!MoreArrays.isSorted(keys, 0, size)) {
            throw new IllegalArgumentException("item array not sorted");
        }
        return new MutableSparseVector(keys, values, size);
    }

    /**
     * Wrap key and value array lists in a mutable sparse vector. Don't modify
     * the original lists once this has been called!
     *
     * @param keyList   The list of keys
     * @param valueList The list of values
     * @return A backed by the backing stores of the provided lists.
     */
    public static MutableSparseVector wrap(LongArrayList keyList, DoubleArrayList valueList) {
        if (valueList.size() < keyList.size()) {
            throw new IllegalArgumentException("Value list too short");
        }

        long[] keys = keyList.elements();
        double[] values = valueList.elements();

        if (!MoreArrays.isSorted(keys, 0, keyList.size())) {
            throw new IllegalArgumentException("key array not sorted");
        }

        return new MutableSparseVector(keys, values, keyList.size());
    }

    /**
     *  Remove the channel stored under a particular symbol.
     *
     * @param channelSymbol the symbol under which the channel was
     * stored in the vector.
     * @return the channel, which is itself a sparse vector.
     * @throws IllegalArgumentException if this vector does not have
     * such a channel at this time.
     */
    public SparseVector removeChannel(Symbol channelSymbol) {
	checkMutable();
	SparseVector retval;
	if (hasChannel(channelSymbol)) {
	    retval = channelMap.remove(channelSymbol);
	    return retval;
	}
	throw new IllegalArgumentException("No existing channel under name " +
					   channelSymbol.getName());
    }

    /**
     *  Remove all channels stored in this vector.
     */
    public void removeAllChannels() {
	checkMutable();
	channelMap.clear();
    }	

    /**
     * Add a channel to thie vector.  The new channel will be empty,
     * and will have the same key domain as this vector.
     *
     * @param channelSymbol the symbol under which this new channel
     * should be created.
     * @returns the newly created channel
     * @throws IllegalArgumentException if there is already a channel
     * with that symbol
     */
    public MutableSparseVector addChannel(Symbol channelSymbol) {
	checkMutable();
	if (hasChannel(channelSymbol)) {
	    throw new IllegalArgumentException("There is already a channel with name " +
					       channelSymbol.getName());
	}
	MutableSparseVector theChannel = new MutableSparseVector(keyDomain());
	channelMap.put(channelSymbol, theChannel);
	return theChannel;
    }

    /**
     * Add a channel to thie vector, even if there is already a
     * channel with the same symbol.  The new channel will be empty,
     * and will have the same key domain as this vector.
     *
     * @param channelSymbol the symbol under which this new channel
     * should be created.
     * @returns the newly created channel
     */
    public MutableSparseVector alwaysAddChannel(Symbol channelSymbol) {
	if (! hasChannel(channelSymbol)) {
	    addChannel(channelSymbol);
	}
	return channelMap.get(channelSymbol);
    }
	

    /**
     * Add a channel to thie vector, and set it equal to a given
     * value.  The input channel must have a compatible key domain to
     * this channel.  The input channel should not be changed after it
     * is passed in to this method.
     *
     * @param channelSymbol the symbol under which this new channel
     * should be created.
     * @returns the newly created channel
     * @throws IllegalArgumentException if there is already a channel
     * with that symbol
     */
    public MutableSparseVector addChannel(Symbol channelSymbol, MutableSparseVector theChannel) {
	checkMutable();
	if (hasChannel(channelSymbol)) {
	    throw new IllegalArgumentException("There is already a channel with name " +
					       channelSymbol.getName());
	}
	if (! this.keyDomain().containsAll(theChannel.keyDomain())) {
	    throw new IllegalArgumentException("The channel you are trying to add to this vector "
					       + "has an incompatible key domain.");
	}
	MutableSparseVector theChannelCopy = theChannel.mutableCopy();
	channelMap.put(channelSymbol, theChannelCopy);
	return theChannelCopy;
    }



    @Override
    public boolean hasChannel(Symbol channelSymbol) {
	return channelMap.containsKey(channelSymbol);
    }

    @Override
    public MutableSparseVector channel(Symbol channelSymbol) {
	if (hasChannel(channelSymbol)) {
	    if (isMutable) return channelMap.get(channelSymbol);
	    else return channelMap.get(channelSymbol).partialFreeze();
	}
	throw new IllegalArgumentException("No existing channel under name " +
					   channelSymbol.getName());
    }
    
}
