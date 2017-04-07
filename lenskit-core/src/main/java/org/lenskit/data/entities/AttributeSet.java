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

import com.google.common.collect.Iterators;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A set of attributes.  Attributes are mapped to positions.
 */
public class AttributeSet extends AbstractSet<TypedName<?>> {
    // Typed names are always interned, so we can use == to compare them.
    private final TypedName<?>[] names;
    private transient Set<String> nameSet;

    AttributeSet(TypedName<?>[] ans) {
        names = ans;
    }

    /**
     * Create a new attribute set.
     * @param names The list of attributes.
     * @return The attribute set.
     */
    public static AttributeSet create(TypedName<?>... names) {
        return new AttributeSet(names);
    }

    /**
     * Look up an attribute.
     * @param name The attribute.
     * @return The attribute's index, or a negative value if it does not exist.  Nonexistence is further
     * differentiated with -1 for no attribute, -2 for type mismatch.
     */
    public int lookup(TypedName<?> name) {
        // Linear search with interned objects is faster for short lists
        for (int i = 0; i < names.length; i++) {
            TypedName<?> n = names[i];
            if (n == name) {
                return i;
            } else if (n.getName() == name.getName()) {
                // FIXME Handle typecasting
                return -2;
            }
        }
        return -1;
    }

    /**
     * Look up an attribute.
     * @param name The attribute name.
     * @return The attribute's index, or a negative value if it does not exist.
     */
    public int lookup(String name) {
        name = name.intern();

        for (int i = 0; i < names.length; i++) {
            TypedName<?> n = names[i];
            if (n.getName() == name) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Iterator<TypedName<?>> iterator() {
        return new TNIter();
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof TypedName && lookup((TypedName<?>) o) >= 0;
    }

    /**
     * Get the number of attributes in this set.
     * @return The number of attributes in this set.
     */
    @Override
    public int size() {
        return names.length;
    }

    /**
     * Get a set-of-strings view of this attribute set.
     * @return The A set-of-strings view of the attribute set.
     */
    public Set<String> nameSet() {
        Set<String> s = nameSet;
        if (s == null) {
            nameSet = s = new NameSet();
        }
        return s;
    }

    private class NameSet extends AbstractSet<String> {
        @Override
        public boolean contains(Object o) {
            return o instanceof String && lookup((String) o) >= 0;
        }

        @Override
        public Iterator<String> iterator() {
            return Iterators.transform(new TNIter(), TypedName::getName);
        }

        @Override
        public int size() {
            return names.length;
        }
    }

    private class TNIter implements Iterator<TypedName<?>> {
        private int pos = 0;

        @Override
        public boolean hasNext() {
            return pos < names.length;
        }

        @Override
        public TypedName<?> next() {
            if (pos >= names.length) {
                throw new NoSuchElementException();
            }
            return names[pos++];
        }
    }
}
