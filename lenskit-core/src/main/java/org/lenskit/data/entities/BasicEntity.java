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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of an entity backed by a basic set of attributes.
 */
@Immutable
class BasicEntity extends AbstractEntity {
    private final Map<String, Attribute<?>> attributes;

    BasicEntity(EntityType t, long eid, Map<String, Attribute<?>> attrs) {
        super(t, eid);
        attributes = ImmutableMap.<String,Attribute<?>>builder()
                                 .put(CommonAttributes.ENTITY_ID.getName(),
                                      Attribute.create(CommonAttributes.ENTITY_ID, eid))
                                 .putAll(attrs)
                                 .build();
        assert id == (Long) attributes.get(CommonAttributes.ENTITY_ID.getName())
                                      .getValue();
    }

    @Override
    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    @Override
    public Set<TypedName<?>> getTypedAttributeNames() {
        // TODO Make this more efficient
        ImmutableSet.Builder<TypedName<?>> bld = ImmutableSet.builder();
        for (Attribute<?> name: attributes.values()) {
            bld.add(name.getTypedName());
        }
        return bld.build();
    }

    @Override
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    @Override
    public boolean hasAttribute(TypedName<?> name) {
        return attributes.containsKey(name.getName());
    }

    @Nullable
    @Override
    public <T> T maybeGet(TypedName<T> name) {
        Attribute<?> attr = attributes.get(name.getName());
        if (attr == null) {
            return null;
        } else {
            Class<T> type = name.getType();
            Object value = attr.getValue();
            if (type.isInstance(value)) {
                return type.cast(value);
            } else {
                return null;
            }
        }
    }

    @Nullable
    @Override
    public Object maybeGet(String name) {
        Attribute<?> attr = attributes.get(name);
        return attr != null ? attr.getValue() : null;
    }
}
