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

/**
 * Definitions and utilities for common fields.
 */
public final class CommonAttributes {
    private CommonAttributes() {}

    /**
     * Attribute indicating the entity ID.
     */
    public static final TypedName<Long> ENTITY_ID = TypedName.create("id", Long.class);

    /**
     * The user ID associated with an entity.  This is for when the user is a *foreign key*; in user
     * entities, the user ID is stored as the entity ID.
     */
    public static final TypedName<Long> USER_ID = TypedName.create("user", Long.class);
    /**
     * The item ID associated with an entity. This is for when the user is a *foreign key*; in item
     * entities, the item ID is stored as the entity ID.
     */
    public static final TypedName<Long> ITEM_ID = TypedName.create("item", Long.class);
    /**
     * A name associated with the entity.
     */
    public static final TypedName<String> NAME = TypedName.create("name", String.class);

    /**
     * A timestamp associated with an event entity.
     */
    public static final TypedName<Long> TIMESTAMP = TypedName.create("timestamp", Long.class);
    /**
     * A rating value.
     */
    public static final TypedName<Double> RATING = TypedName.create("rating", Double.class);
    /**
     * A standard count, for events that may use them.
     */
    public static final TypedName<Integer> COUNT = TypedName.create("count", Integer.class);
}
