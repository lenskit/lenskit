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
    private final EntityType type;
    private final long id;
    private final Map<Attribute<?>, Object> attributes;

    public BasicEntity(EntityType t, long eid, Map<Attribute<?>, Object> attrs) {
        type = t;
        id = eid;
        attributes = ImmutableMap.<Attribute<?>,Object>builder()
                                 .put(CommonAttributes.ENTITY_ID, eid)
                                 .putAll(attrs)
                                 .build();
        assert id == (Long) attributes.get(CommonAttributes.ENTITY_ID);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public EntityType getType() {
        return type;
    }

    @Override
    public Set<String> getAttributeNames() {
        // FIXME Make this more efficient
        ImmutableSet.Builder<String> names = ImmutableSet.builder();
        for (Attribute<?> attr: attributes.keySet()) {
            names.add(attr.getName());
        }
        return names.build();
    }

    @Override
    public Set<Attribute<?>> getAttributes() {
        return attributes.keySet();
    }

    @Override
    public boolean hasAttribute(String name) {
        return getAttributeNames().contains(name);
    }

    @Override
    public boolean hasAttribute(Attribute<?> attribute) {
        return attributes.containsKey(attribute);
    }

    @Nullable
    @Override
    public <T> T maybeGet(Attribute<T> attribute) {
        return attribute.getType().cast(attributes.get(attribute));
    }

    @Nullable
    @Override
    public Object maybeGet(String attr) {
        for (Map.Entry<Attribute<?>, Object> e: attributes.entrySet()) {
            if (e.getKey().getName().equals(attr)) {
                return e.getValue();
            }
        }
        return null;
    }
}
