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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * General-purpose builder for {@linkplain Entity entities}.
 */
public class BasicEntityBuilder extends EntityBuilder {
    private Map<String,Attribute<?>> attributes;

    /**
     * Create a new entity builder for a type.
     * @param type The entity type.
     */
    public BasicEntityBuilder(EntityType type) {
        super(type);
        attributes = new HashMap<>();
    }

    @Override
    public <T> EntityBuilder setAttribute(TypedName<T> name, T val) {
        Preconditions.checkNotNull(name, "attribute");
        Preconditions.checkNotNull(val, "value");
        Preconditions.checkArgument(name.getType().isInstance(val),
                                    "value %s not of type %s", val, name.getType());
        if (name == CommonAttributes.ENTITY_ID) {
            return setId(((Long) val).longValue());
        } else {
            attributes.put(name.getName(), Attribute.create(name, val));
            return this;
        }
    }

    @Override
    public EntityBuilder clearAttribute(TypedName<?> name) {
        Preconditions.checkNotNull(name, "attribute");
        attributes.remove(name);
        if (name == CommonAttributes.ENTITY_ID) {
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
