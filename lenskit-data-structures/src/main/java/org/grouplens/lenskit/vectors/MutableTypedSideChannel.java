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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import org.grouplens.lenskit.collections.LongKeyDomain;

import java.util.Arrays;

/**
 * Add modification operations to a side channel.  Since we do not expose these implementations
 * outside the vectors package, we don't need the third Immutable type.
 *
 * @param <V> The type stored in the channel.
 */
class MutableTypedSideChannel<V> extends TypedSideChannel<V> {
    private static final long serialVersionUID = 1L;

    /**
     * Build a new mutable typed channel from the given keys and values.
     *
     * @param ks The key set backing this map.
     * @param vs The array of values backing this vector.
     * @param dft The initial {@link #defaultReturnValue()}.
     */
    MutableTypedSideChannel(LongKeyDomain ks, V[] vs, V dft) {
        super(ks, vs, dft);
    }

    MutableTypedSideChannel(LongKeyDomain ks, V[] vs) {
        super(ks, vs);
    }
    
    /**
     * Build a new mutable typed channel from the given keys.  All keys will be initially deactivated.
     * 
     * @param ks The keys.
     */
    @SuppressWarnings("unchecked")
    MutableTypedSideChannel(LongKeyDomain ks) {
        super(ks);
    }

    /**
     * Check if the vector is mutable.
     */
    protected void checkMutable() {
        if (frozen) {
            throw new IllegalStateException("side channel is frozen");
        }
    }
    
    /**
     * Returns this object, which may safely be used after calling this function.
     */
    @Override
    public TypedSideChannel<V> immutable() {
         return new TypedSideChannel<V>(keys.clone(),
                                        Arrays.copyOf(values, keys.domainSize()),
                                        defaultReturnValue());
    }

    /**
     * Create a copy with the specified domain.  If {@code freeze} is set, also try to re-use storage
     * and render this vector inoperable.
     *
     * @param domain The domain to use.
     * @param freeze If {@code true}, try to re-use this storage. If this option is set, then this
     *               map will be unusable after this method has been called.
     * @return The new, immutable map.
     */
    public TypedSideChannel<V> immutable(LongKeyDomain domain, boolean freeze) {
        LongKeyDomain nks = domain.clone();
        V[] nvs;
        nvs = adjustStorage(nks, freeze);
        frozen |= freeze;
        return new TypedSideChannel<V>(nks, nvs, defaultReturnValue());
    }

    /**
     * Create a copy with the specified domain.
     *
     * @param domain The domain to use.
     * @return The new map.
     */
    public MutableTypedSideChannel<V> withDomain(LongKeyDomain domain) {
        LongKeyDomain nks = domain.clone();
        V[] nvs = adjustStorage(nks, false);
        MutableTypedSideChannel<V> copy = new MutableTypedSideChannel<V>(nks, nvs, defaultReturnValue());
        copy.defaultReturnValue(defaultReturnValue());
        return copy;
    }

    private V[] adjustStorage(LongKeyDomain domain, boolean reuseIfPossible) {
        V[] nvs;
        if (domain.isCompatibleWith(keys)) {
            nvs = reuseIfPossible ? values : Arrays.copyOf(values, domain.domainSize());
            domain.setActive(keys.getActiveMask());
        } else {
            nvs = (V[]) new Object[domain.domainSize()];

            int i = 0;
            int j = 0;
            final int end = keys.domainSize();
            while (i < nvs.length && j < end) {
                final long ki = domain.getKey(i);
                final long kj = keys.getKey(j);
                if (ki == kj) {
                    nvs[i] = values[j];
                    domain.setActive(i, keys.indexIsActive(j));
                    i++;
                    j++;
                } else if (kj < ki) {
                    j++;
                } else {
                    domain.setActive(i, false);
                    i++;
                }
            }
        }
        return nvs;
    }

    @Override
    public void clear() {
        checkMutable();
        keys.setAllActive(false);
        ObjectArrays.fill(values, null);
    }

    @Override
    public V put(long key, V value) {
        Preconditions.checkNotNull(value, "channel values cannot be null");
        checkMutable();
        final int idx = keys.getIndex(key);
        if(idx >= 0) {
            V retval = keys.indexIsActive(idx) ? values[idx] : defaultReturnValue();
            values[idx] = value;
            keys.setActive(idx, true);
            return retval;
        } else {
            throw new IllegalArgumentException("key " + key + " not in key domain");
        }
    }

    @Override
    public V remove(long key) {
        checkMutable();
        final int idx = keys.getIndex(key);
        V retval = defaultReturnValue();
        if(idx >= 0) {
            if (keys.indexIsActive(idx)) {
                retval = values[idx];
            }
            keys.setActive(idx, false);
            values[idx] = null;
        }
        return retval;
    }

    @Override
    public void defaultReturnValue(V rv) {
        checkMutable();
        defRetValue = rv;
    }

    /**
     * used to mark a side channel as frozen without all the busywork of freezing it.
     * Currently used for returning side channels from frozen sets.
     */
    TypedSideChannel<V> partialFreeze() {
        frozen = true;
        return this;
    }
}
