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

import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for entity builders using bean-style setters.  An entity builder should extend this class, then annotate
 * setter methods with {@link EntityAttributeSetter} (and clearers with {@link EntityAttributeClearer}).  The standard
 * methods will then be implemented in terms of these.
 */
public class AbstractBeanEntityBuilder extends EntityBuilder {
    private static final ConcurrentHashMap<Class<? extends AbstractBeanEntityBuilder>, AttrEntry[]> cache =
            new ConcurrentHashMap<>();

    private final AttrEntry[] attributeRecords;

    protected AbstractBeanEntityBuilder(EntityType type) {
        super(type);
        attributeRecords = lookupAttrs(getClass());
    }

    @Nullable
    private final AttrEntry findEntry(TypedName<?> name) {
        for (AttrEntry e: attributeRecords) {
            if (e.name == name) {
                return e;
            }
        }

        return null;
    }

    @Override
    public <T> EntityBuilder setAttribute(TypedName<T> name, T val) {
        AttrEntry e = findEntry(name);
        if (e == null) {
            setExtraAttribute(name, val);
        } else {
            try {
                e.setter.invoke(this, val);
            } catch (IllegalAccessException | InvocationTargetException e1) {
                throw new RuntimeException("error invoking " + e.setter, e1);
            }
        }

        return this;
    }

    @Override
    public EntityBuilder clearAttribute(TypedName<?> name) {
        AttrEntry e = findEntry(name);

        try {
            if (e == null) {
            clearExtraAttribute(name);
            } else if (e.clearer != null) {
                e.clearer.invoke(this);
            } else {
                e.setter.invoke(null);
            }
        } catch (IllegalAccessException | InvocationTargetException e1) {
            throw new RuntimeException("error invoking " + e.setter, e1);
        }

        return this;
    }

    /**
     * Set an extra attribute. An extra attribute is an attribute that is not provided by a bean-style setter.
     *
     * The default implementation throws {@link NoSuchAttributeException}.
     *
     * @param name The attribute name to clear.
     * @param val The attribute value to set.
     */
    public <T> void setExtraAttribute(TypedName<T> name, T val) {
        throw new NoSuchAttributeException(name.toString());
    }

    /**
     * Clear an extra attribute. An extra attribute is an attribute that is not provided by a bean-style setter.
     *
     * The default implementation does nothing.
     *
     * @param name The attribute name to clear.
     */
    public void clearExtraAttribute(TypedName<?> name) {

    }

    @Override
    public Entity build() {
        return null;
    }

    private static AttrEntry[] lookupAttrs(Class<? extends AbstractBeanEntityBuilder> type) {
        AttrEntry[] res = cache.get(type);
        if (res != null) {
            return res;
        }

        Map<String,AttrEntry> attrs = new HashMap<>();
        Map<String,Method> clearers = new HashMap<>();
        for (Method m: type.getMethods()) {
            EntityAttributeSetter annot = m.getAnnotation(EntityAttributeSetter.class);
            if (annot != null) {
                m.setAccessible(true);
                Type[] params = m.getParameterTypes();
                if (params.length != 1) {
                    throw new IllegalArgumentException("method " + m + " has " + params.length + " parameters, expected 1");
                }
                TypeToken atype = TypeToken.of(params[0]);
                TypedName<?> name = TypedName.create(annot.value(), atype);
                AttrEntry e = new AttrEntry(name);
                e.setter = m;
                attrs.put(annot.value(), e);
            }
            EntityAttributeClearer clearAnnot = m.getAnnotation(EntityAttributeClearer.class);
            if (clearAnnot != null) {
                m.setAccessible(true);
                clearers.put(clearAnnot.value(), m);
            }
        }

        for (Map.Entry<String,Method> ce: clearers.entrySet()) {
            AttrEntry ae = attrs.get(ce.getKey());
            if (ae == null) {
                throw new RuntimeException("clearer " + ce.getValue() + " for " + ce.getKey() + " has no corresponding setter");
            }
            ae.clearer = ce.getValue();
        }

        AttrEntry[] ael = attrs.values().toArray(new AttrEntry[attrs.size()]);
        cache.put(type, ael);
        return ael;
    }

    private static class AttrEntry {
        final TypedName<?> name;
        Method setter;
        Method clearer;

        AttrEntry(TypedName<?> n) {
            name = n;
        }
    }
}
