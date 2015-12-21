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

import java.util.HashMap;
import java.util.Map;

/**
 * General-purpose builder for {@linkplain Entity entities}.
 */
public class EntityBuilder {
    private EntityType type;
    private long id;
    private boolean idSet;
    private Map<Attribute<?>, Object> attributes;

    /**
     * Create a new, empty entity builder.
     */
    public EntityBuilder() {
        this(null, -1, false, new HashMap<Attribute<?>, Object>());
    }

    private EntityBuilder(EntityType typ, long initId, boolean initIdSet, Map<Attribute<?>, Object> attrs) {
        type = typ;
        id = initId;
        idSet = initIdSet;
        attributes = attrs;
    }

    /**
     * Create a new, empty entity builder.
     * @return A new, empty entity buidler.
     */
    public static EntityBuilder create() {
        return new EntityBuilder();
    }

    /**
     * Create a new entity builder with the specified ID and type.
     * @param type The entity type.
     * @param id The entity ID.
     */
    public static EntityBuilder create(EntityType type, long id) {
        return new EntityBuilder(type, id, true, new HashMap<Attribute<?>, Object>());
    }

    /**
     * Create a new entity builder with a specified type.
     * @param type The entity type.
     */
    public static EntityBuilder create(EntityType type) {
        return new EntityBuilder(type, -1, false, new HashMap<Attribute<?>, Object>());
    }

    /**
     * Create a new entity builder that is initialized with a copy of an entity.
     * @param e The entity.
     * @return An entity builder initialized to build a copy of {@code e}.
     */
    public static EntityBuilder copy(Entity e) {
        EntityBuilder eb = create(e.getType(), e.getId());
        for (Attribute a: e.getAttributes()) {
            eb.setAttribute(a, e.get(a));
        }
        return eb;
    }

    /**
     * Set the entity type.
     * @param typ The entity type.
     * @return The entity builder (for chaining).
     */
    public EntityBuilder setType(EntityType typ) {
        type = typ;
        return this;
    }

    /**
     * Set the entity id.
     * @param eid The entity id.
     * @return The entity builder (for chaining).
     */
    public EntityBuilder setId(long eid) {
        id = eid;
        idSet = true;
        return this;
    }

    /**
     * Set an attribute for the entity.
     * @param attr The attribute.
     * @param val The value. Cannot be `null`.
     * @param <T> The attribute type.
     * @return The entity builder (for chaining).
     */
    public <T> EntityBuilder setAttribute(Attribute<T> attr, T val) {
        Preconditions.checkNotNull(attr, "attribute");
        Preconditions.checkNotNull(val, "value");
        attributes.put(attr, val);
        return this;
    }

    /**
     * Clear an attribute for the entity
     * @param attr The attribute.
     * @return The entity builder (for chaining).
     */
    public EntityBuilder clearAttribute(Attribute<?> attr) {
        Preconditions.checkNotNull(attr, "attribute");
        attributes.remove(attr);
        return this;
    }

    public Entity build() {
        Preconditions.checkState(type != null, "Entity type not set");
        Preconditions.checkState(idSet, "ID not set");
        if (attributes.isEmpty()) {
            return new BareEntity(type, id);
        } else {
            return null;
        }
    }
}
