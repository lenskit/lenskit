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
