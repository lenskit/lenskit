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

import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.util.TypeUtils;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.*;

/**
 * An association of a type with a name.  This is used for type-safe (or at least type-suggested) access to entity
 * attributes, and allows declarations of attribute names to also contain the type of data that will be stored in
 * the specified attribute.
 *
 * When used consistently, they allow for type-safe access to entity attribute data.
 */
@Immutable
public final class TypedName<T> implements Serializable {
    private static final long serialVersionUID = -1L;
    private static final Interner<TypedName<?>> FIELD_CACHE = Interners.newStrongInterner();

    private final String name;
    private final TypeToken<T> type;
    private transient volatile int hashCode;
    private transient volatile StringConverter<T> converter;


    private TypedName(String n, TypeToken<T> t) {
        name = n;
        type = t;
    }

    /**
     * Get the underlying name.
     * @return The name.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Get the type.  This will never be a primitive type class; primitive classes are
     * always normalized to their wrapper classes (e.g. `long.class` becomes `Long.class`).
     *
     * @return The type.
     */
    @Nonnull
    public TypeToken<T> getType() {
        return type;
    }

    /**
     * Get the raw type (class). This will never be a primitive type class; primitive classes are
     * always normalized to their wrapper classes (e.g. `long.class` becomes `Long.class`).
     *
     * @return The type.
     */
    @Nonnull
    public Class<? super T> getRawType() {
        return type.getRawType();
    }

    /**
     * Parse a string containing this attribute's value.
     * @return The attribute value.
     */
    public T parseString(String value) {
        // FIXME Handle list types
        StringConverter cvt = converter;
        if (cvt == null) {
            converter = cvt = StringConvert.INSTANCE.findConverter(type.getRawType());
        }
        return (T) cvt.convertFromString(type.getRawType(), value);
    }


    @Override
    public int hashCode() {
        // intermediate variable means only 1 memory fence in common case
        int hc = hashCode;
        if (hc == 0) {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hc = hcb.append(name)
                    .append(type)
                    .toHashCode();
            hashCode = hc;
        }
        return hc;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof TypedName) {
            TypedName of = (TypedName) o;
            return name.equals(of.getName()) && type.equals(of.getType());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String tname;
        if (ClassUtils.isPrimitiveWrapper(type.getRawType())) {
            tname = ClassUtils.wrapperToPrimitive(type.getRawType()).getName();
        } else {
            tname = type.toString();
        }
        return "TypedName[" + name + ": " + tname + "]";
    }

    /**
     * Create a typed name object.
     *
     * @param name The name.
     * @param type The type.
     * @return An object encapsulating the specified name and type.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> TypedName<T> create(String name, Class<T> type) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(type, "type");
        if (type.isPrimitive()) {
            type = (Class<T>) ClassUtils.primitiveToWrapper(type);
        }
        TypedName<T> attribute = new TypedName<>(name.intern(), TypeToken.of(type));
        return (TypedName<T>) FIELD_CACHE.intern(attribute);
    }

    /**
     * Create a typed name object.
     *
     * @param name The name.
     * @param type The type.
     * @return An object encapsulating the specified name and type.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> TypedName<T> create(String name, TypeToken<T> type) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(type, "type");
        if (type.isPrimitive()) {
            type = TypeToken.of((Class<T>) ClassUtils.primitiveToWrapper(type.getRawType()));
        }
        TypedName<T> attribute = new TypedName<>(name.intern(), type);
        return (TypedName<T>) FIELD_CACHE.intern(attribute);
    }

    /**
     * Create an typed name from a name and type name.
     * @param name The name.
     * @param typeName The type name.
     * @return The attribute.
     * @throws IllegalArgumentException if `typeName` is not a valid type name.
     */
    public static TypedName<?> create(String name, String typeName) {
        Class<?> type;
        type = TypeUtils.resolveTypeName(typeName);

        return create(name, type);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new InvalidObjectException("typed names must use serialization proxy");
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("typed names must use serialization proxy");
    }

    private Object writeReplace() {
        return new SerialProxy(name, type);
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 2L;

        private String name;
        private TypeToken type;

        public SerialProxy(String n, TypeToken t) {
            name = n;
            type = t;
        }

        private Object readResolve() throws ObjectStreamException {
            return create(name, type);
        }
    }
}
