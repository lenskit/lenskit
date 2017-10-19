/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.data.entities;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lenskit.util.describe.Describable;
import org.lenskit.util.describe.DescriptionWriter;

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
    @EntityAttribute("id")
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
        return value != null && name.getRawType().isInstance(value);
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
            // FIXME This is not fully type-safe!
            return (T) name.getRawType().cast(maybeGet(name.getName()));
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
           .append(getId());
        for (Attribute<?> av: getAttributes()) {
            hcb.append(av.getName()).append(av.getValue());
        }
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
            return new EntryIter(getTypedAttributeNames().iterator());
        }

        @Override
        public int size() {
            return getAttributeNames().size();
        }
    }

    private class EntryIter implements Iterator<Map.Entry<String,Object>> {
        private final Iterator<TypedName<?>> baseIter;

        public EntryIter(Iterator<TypedName<?>> keyIter) {
            baseIter = keyIter;
        }

        @Override
        public boolean hasNext() {
            return baseIter.hasNext();
        }

        @Override
        public Map.Entry<String, Object> next() {
            TypedName<?> attr = baseIter.next();
            return ImmutablePair.of(attr.getName(), get(attr));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
