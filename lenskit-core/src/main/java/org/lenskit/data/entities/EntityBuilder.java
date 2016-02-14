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

/**
 * Base class for entity builders.  The standard implementation is {@link BasicEntityBuilder}, which can be created
 * with {@link Entities#newBuilder()}.
 */
public abstract class EntityBuilder {
    protected EntityType type;
    protected long id;
    protected boolean idSet;

    /**
     * Construct a new entity builder.
     *
     * **Note:** This constructor calls {@link #setId(long)} and {@link #setType(EntityType)}, so if those methods
     * are overloaded, they should be able to run without the derived class constructor having finished.
     *
     * @param initId The initial ID.
     * @param initIdSet Whether the ID is initially set.
     * @param typ The entity type.
     */
    protected EntityBuilder(long initId, boolean initIdSet, EntityType typ) {
        if (initIdSet) {
            setId(initId);
        }
        setType(typ);
    }

    /**
     * Set the entity type.
     * @param typ The entity type.
     * @return The entity builder (for chaining).
     * @throws IllegalArgumentException if the type is not valid for this builder.
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
     * Set an attribute in the entity.
     * @param attr The attribute to set.
     * @param val The attribute value.
     * @param <T> The attribute type.
     * @return The entity builder (for chaining).
     * @throws NoSuchAttributeException if the specified attribute is not supported by this entity.
     */
    public abstract <T> EntityBuilder setAttribute(Attribute<T> attr, T val);

    /**
     * Clear an attribute.
     * @param attr The attribute to clear.
     * @return The entity builder (for chaining).
     */
    public abstract EntityBuilder clearAttribute(Attribute<?> attr);

    /**
     * Build the entity.
     * @return The entity to build.
     */
    public abstract Entity build();
}
