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

import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * Base class for entity builders.  The standard implementation is {@link BasicEntityBuilder}, which can be created
 * with {@link Entities#newBuilder(EntityType)}.
 */
public abstract class EntityBuilder {
    protected final EntityType type;
    protected long id;
    protected boolean idSet;

    /**
     * Construct a new entity builder.
     *
     * @param typ The entity type.
     */
    protected EntityBuilder(EntityType typ) {
        type = typ;
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
     * @param name The name of the attribute to set.
     * @param val The attribute value.
     * @param <T> The attribute type.
     * @return The entity builder (for chaining).
     * @throws NoSuchAttributeException if the specified attribute is not supported by this entity.
     */
    public abstract <T> EntityBuilder setAttribute(TypedName<T> name, T val);

    /**
     * Set an attribute in the entity.
     * @param attr The attribute to set.
     * @param <T> The attribute type.
     * @return The entity builder (for chaining).
     * @throws NoSuchAttributeException if the specified attribute is not supported by this entity.
     */
    public <T> EntityBuilder setAttribute(Attribute<T> attr) {
        setAttribute(attr.getTypedName(), attr.getValue());
        return this;
    }

    /**
     * Set an attribute in the entity.
     * @param name The name of the attribute to set.
     * @param val The attribute value.
     * @return The entity builder (for chaining).
     * @throws NoSuchAttributeException if the specified attribute is not supported by this entity.
     */
    public EntityBuilder setLongAttribute(TypedName<Long> name, long val) {
        return setAttribute(name, val);
    }

    /**
     * Set an attribute in the entity.
     * @param name The name of the attribute to set.
     * @param val The attribute value.
     * @return The entity builder (for chaining).
     * @throws NoSuchAttributeException if the specified attribute is not supported by this entity.
     */
    public EntityBuilder setDoubleAttribute(TypedName<Double> name, double val) {
        return setAttribute(name, val);
    }

    /**
     * Clear an attribute.
     * @param name The name of the attribute to clear.
     * @return The entity builder (for chaining).
     */
    public abstract EntityBuilder clearAttribute(TypedName<?> name);

    /**
     * Reset this entity builder, clearing all properties **except** the entity type.
     * @return The builder (for chaining).
     */
    @OverridingMethodsMustInvokeSuper
    public EntityBuilder reset() {
        idSet = false;
        return this;
    }

    /**
     * Build the entity.
     * @return The entity to build.
     */
    public abstract Entity build();
}
