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

import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.grapht.util.ClassProxy;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * Identifier for a field in an entity.
 */
public final class Field<T> implements Serializable {
    private static final long serialVersionUID = -1L;
    private static final Interner<Field<?>> FIELD_CACHE = Interners.newStrongInterner();

    private final String name;
    private final Class<T> type;
    private transient volatile int hashCode;

    private Field(String n, Class<T> t) {
        name = n;
        type = t;
    }

    /**
     * Get the field's name.
     * @return The field's name.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Get the field's type.  Primitive field types are normalized to their wrapper types, so this is always a reference
     * type.
     *
     * @return The field's type.
     */
    @Nonnull
    public Class<T> getType() {
        return type;
    }

    @Override
    public int hashCode() {
        // intermediate variable means only 1 memory fence in common case
        int hc = hashCode;
        if (hc == 0) {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hashCode = hc = hcb.append(name)
                               .append(type)
                               .toHashCode();
        }
        return hc;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Field) {
            Field of = (Field) o;
            return name.equals(of.getName()) && type.equals(of.getType());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Field[" + name + ", type=" + type.getCanonicalName() + "]";
    }

    /**
     * Create a field object.
     *
     * @param name The field name.
     * @param type The field type.
     * @return An object encapsulating the specified field.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> Field<T> create(String name, Class<T> type) {
        Preconditions.checkNotNull(name, "field name");
        Preconditions.checkNotNull(type, "field type");
        if (type.isPrimitive()) {
            type = (Class<T>) ClassUtils.primitiveToWrapper(type);
        }
        Field<T> field = new Field<>(name, type);
        return (Field<T>) FIELD_CACHE.intern(field);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new InvalidObjectException("fields must use serialization proxy");
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("fields must use serialization proxy");
    }

    private Object writeReplace() {
        return new SerialProxy(name, ClassProxy.of(type));
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private ClassProxy type;

        public SerialProxy(String n, ClassProxy t) {
            name = n;
            type = t;
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return create(name, type.resolve());
            } catch (ClassNotFoundException e) {
                throw new InvalidObjectException("Cannot resolve type " + type.getClassName());
            }
        }
    }
}
