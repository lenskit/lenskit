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
package org.lenskit.data.entities;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Base class to make it easier to implement entities.
 */
public abstract class AbstractEntity implements Entity {
    @Override
    public Map<String, Object> asMap() {
        return new MapView();
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #maybeGet(Attribute)}.
     */
    @Nonnull
    @Override
    public <T> T get(Attribute<T> attribute) {
        T val = maybeGet(attribute);
        if (val == null) {
            throw new NoSuchAttributeException(attribute.toString());
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
     * This implementation delegates to {@link #get(Attribute)}.
     */
    @Override
    public long getLong(Attribute<Long> attribute) {
        return get(attribute);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #get(Attribute)}.
     */
    @Override
    public double getDouble(Attribute<Double> attr) {
        return get(attr);
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
