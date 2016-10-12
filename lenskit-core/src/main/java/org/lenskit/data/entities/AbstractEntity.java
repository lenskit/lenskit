/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.data.entities;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.grouplens.lenskit.util.io.Describable;
import org.grouplens.lenskit.util.io.DescriptionWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Base class to make it easier to implement entities.
 */
public abstract class AbstractEntity implements Entity, Describable {
    protected final EntityType type;
    protected final long id;

    protected AbstractEntity(EntityType t, long eid) {
        type = t;
        id = eid;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public EntityType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #hasAttribute(String)}
     */
    @Override
    public boolean hasAttribute(TypedName<?> name) {
        Object value = maybeGet(name.getName());
        return value != null && name.getType().isInstance(value);
    }

    /**
     * {@inheritDoc}
     *
     * Delegates to {@link #getTypedAttributeNames()} and extracts the names.
     */
    @Override
    public Set<String> getAttributeNames() {
        // TODO Make this more efficient
        ImmutableSet.Builder<String> bld = ImmutableSet.builder();
        for (TypedName<?> name: getTypedAttributeNames()) {
            bld.add(name.getName());
        }
        return bld.build();
    }

    /**
     * {@inheritDoc}
     *
     * Delegates to {@link #getTypedAttributeNames()} and extracts the names.
     */
    @Override
    public Collection<Attribute<?>> getAttributes() {
        return new ValueCollection();
    }

    @Override
    public Map<String, Object> asMap() {
        return new MapView();
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #maybeGet(TypedName)}.
     */
    @Nonnull
    @Override
    public <T> T get(TypedName<T> name) {
        T val = maybeGet(name);
        if (val == null) {
            throw new NoSuchAttributeException(name.toString());
        } else {
            return val;
        }
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #maybeGet(String)}.
     */
    @Nonnull
    @Override
    public Object get(String attr) {
        Object val = maybeGet(attr);
        if (val == null) {
            throw new NoSuchAttributeException(attr);
        } else {
            return val;
        }
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #get(TypedName)}.
     */
    @Override
    public long getLong(TypedName<Long> name) {
        return get(name);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #get(TypedName)}.
     */
    @Override
    public double getDouble(TypedName<Double> name) {
        return get(name);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #get(TypedName)}.
     */
    @Override
    public int getInteger(TypedName<Integer> name) {
        return get(name);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #get(TypedName)}.
     */
    @Override
    public boolean getBoolean(TypedName<Boolean> name) {
        return get(name);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #maybeGet(String)} and checks the type.
     */
    @Nullable
    @Override
    public <T> T maybeGet(TypedName<T> name) {
        try {
            return name.getType().cast(maybeGet(name.getName()));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Entity) {
            Entity oe = (Entity) obj;
            return getType().equals(oe.getType()) && getId() == oe.getId() && asMap().equals(oe.asMap());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(getType())
           .append(getId())
           .append(asMap());
        return hcb.toHashCode();
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("type", getType());
        for (Attribute<?> av: getAttributes()) {
            tsb.append(av.getTypedName().toString(), av.getValue());
        }
        return tsb.toString();
    }

    @Override
    public void describeTo(DescriptionWriter writer) {
        writer.putField("type", getType());
        for (Attribute<?> av: getAttributes()) {
            writer.putField(av.getTypedName().toString(), av.getValue());
        }
    }

    private class MapView extends AbstractMap<String,Object> {
        @Override
        public Set<Entry<String, Object>> entrySet() {
            return new EntrySet();
        }

        @Override
        public int size() {
            return getAttributeNames().size();
        }

        @Override
        public boolean containsKey(Object key) {
            return key instanceof String && hasAttribute((String) key);
        }

        @Override
        public Object get(Object key) {
            if (key instanceof String) {
                return maybeGet((String) key);
            } else {
                return null;
            }
        }

        @Override
        public Set<String> keySet() {
            return getAttributeNames();
        }
    }

    private class ValueCollection extends AbstractCollection<Attribute<?>> {
        @Override
        public Iterator<Attribute<?>> iterator() {
            return Iterators.transform(getTypedAttributeNames().iterator(),
                                       new Function<TypedName<?>, Attribute<?>>() {
                                           @Nullable
                                           @Override
                                           public Attribute<?> apply(@Nullable TypedName<?> input) {
                                               assert input != null;
                                               return Attribute.create((TypedName) input, get(input));
                                           }
                                       });
        }

        @Override
        public int size() {
            return getTypedAttributeNames().size();
        }
    }

    private class EntrySet extends AbstractSet<Map.Entry<String,Object>> {
        @Override
        public Iterator<Map.Entry<String, Object>> iterator() {
            return new EntryIter(getAttributeNames().iterator());
        }

        @Override
        public int size() {
            return getAttributeNames().size();
        }
    }

    private class EntryIter implements Iterator<Map.Entry<String,Object>> {
        private final Iterator<String> baseIter;

        public EntryIter(Iterator<String> keyIter) {
            baseIter = keyIter;
        }

        @Override
        public boolean hasNext() {
            return baseIter.hasNext();
        }

        @Override
        public Map.Entry<String, Object> next() {
            String attr = baseIter.next();
            return ImmutablePair.of(attr, get(attr));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
