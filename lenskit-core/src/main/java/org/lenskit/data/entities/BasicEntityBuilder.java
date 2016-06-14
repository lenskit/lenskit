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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * General-purpose builder for {@linkplain Entity entities}.
 */
public class BasicEntityBuilder extends EntityBuilder {
    private Map<Attribute<?>, Object> attributes;

    /**
     * Create a new, empty entity builder.
     */
    public BasicEntityBuilder() {
        this(null, -1, false, new HashMap<Attribute<?>, Object>());
    }

    /**
     * Create a new entity builder for a type.
     * @param type The entity type.
     */
    public BasicEntityBuilder(EntityType type) {
        this(type, -1, false, new HashMap<Attribute<?>, Object>());
    }

    BasicEntityBuilder(EntityType typ, long initId, boolean initIdSet, Map<Attribute<?>, Object> attrs) {
        super(initId, initIdSet, typ);
        attributes = attrs;
    }

    @Override
    public <T> EntityBuilder setAttribute(Attribute<T> attr, T val) {
        Preconditions.checkNotNull(attr, "attribute");
        Preconditions.checkNotNull(val, "value");
        if (attr == CommonAttributes.ENTITY_ID) {
            return setId(((Long) val).longValue());
        } else {
            attributes.put(attr, val);
            return this;
        }
    }

    @Override
    public EntityBuilder clearAttribute(Attribute<?> attr) {
        Preconditions.checkNotNull(attr, "attribute");
        attributes.remove(attr);
        if (attr == CommonAttributes.ENTITY_ID) {
            idSet = false;
        }
        return this;
    }

    @Override
    public Entity build() {
        Preconditions.checkState(type != null, "Entity type not set");
        Preconditions.checkState(idSet, "ID not set");
        if (attributes.isEmpty() || attributes.keySet().equals(Collections.singleton(CommonAttributes.ENTITY_ID))) {
            return new BareEntity(type, id);
        } else {
            return new BasicEntity(type, id, attributes);
        }
    }
}
