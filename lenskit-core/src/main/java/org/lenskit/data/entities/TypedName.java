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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.ClassUtils;
import org.joda.convert.FromStringConverter;
import org.lenskit.util.TypeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.jcip.annotations.Immutable;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * An association of a type with a name.  This is used for type-safe (or at least type-suggested) access to entity
 * attributes, and allows declarations of attribute names to also contain the type of data that will be stored in
 * the specified attribute.
 *
 * When used consistently, they allow for type-safe access to entity attribute data.
 *
 * Typed names are unique objects, so they can be safely compared with `==`.
 */
@Immutable
public final class TypedName<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Interner<Equivalence.Wrapper<TypedName<?>>> FIELD_CACHE = Interners.newStrongInterner();

    private final String name;
    private final TypeToken<T> type;
    @Nullable
    private transient volatile FromStringConverter converter;
    @Nullable
    private transient volatile Class<? super T> rawType;
    @Nullable
    private transient volatile JavaType javaType;

    private TypedName(String n, TypeToken<T> t) {
        name = n;
        type = t;
    }

    /**
     * Get the underlying name.
     * @return The name. This is guaranteed to be an interned string (see {@link String#intern()}).
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
        Class<? super T> rt = rawType;
        if (rt == null) {
            rawType = rt = type.getRawType();
        }
        return rt;
    }

    /**
     * Get a Jackson {@link JavaType} for this typed name.
     * @return The Jackson java type.
     */
    public JavaType getJacksonType() {
        JavaType jt = javaType;
        if (jt == null) {
            javaType = jt = TypeFactory.defaultInstance().constructType(type.getType());
        }
        return jt;
    }

    /**
     * Parse a string containing this attribute's value.
     * @return The attribute value.
     */
    public T parseString(String value) {
        FromStringConverter cvt = converter;
        if (cvt == null) {
            converter = cvt = TypeUtils.lookupFromStringConverter(type);
        }
        return (T) cvt.convertFromString(getRawType(), value);
    }

    @Override
    public String toString() {
        String tname;
        if (ClassUtils.isPrimitiveWrapper(getRawType())) {
            tname = ClassUtils.wrapperToPrimitive(getRawType()).getName();
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
        return (TypedName<T>) FIELD_CACHE.intern(NAME_EQUIVALENCE.wrap(attribute)).get();
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
        return (TypedName<T>) FIELD_CACHE.intern(NAME_EQUIVALENCE.wrap(attribute)).get();
    }

    /**
     * Create an typed name from a name and type name.
     * @param name The name.
     * @param typeName The type name.
     * @return The attribute.
     * @throws IllegalArgumentException if `typeName` is not a valid type name.
     */
    public static TypedName<?> create(String name, String typeName) {
        return create(name, TypeUtils.resolveTypeName(typeName));
    }

    private Object readResolve() throws ObjectStreamException {
        // Look up the name to ensure singletons
        // This is not entirely safe; see Effective Java, 2nd Ed., #77 for details.
        // However, we do not depend on singletons for security, only for correctness.
        // It is acceptable if malicious serialization streams result in broken objects.
        return create(name, type);
    }

    private static final Equivalence<TypedName<?>> NAME_EQUIVALENCE = new Equivalence<TypedName<?>>() {
        @Override
        protected boolean doEquivalent(TypedName<?> a, TypedName<?> b) {
            return a == b || (a.name.equals(b.getName()) && a.type.equals(b.getType()));
        }

        @Override
        protected int doHash(TypedName<?> typedName) {
            return typedName.name.hashCode() ^ typedName.type.hashCode();
        }
    };
}
