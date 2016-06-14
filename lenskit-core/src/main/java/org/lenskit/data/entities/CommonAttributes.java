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
 * Definitions and utilities for common fields.
 */
public final class CommonAttributes {
    private CommonAttributes() {}

    /**
     * Attribute indicating the entity ID.
     */
    public static final Attribute<Long> ENTITY_ID = Attribute.create("id", Long.class);

    /**
     * The user ID associated with an entity.  This is for when the user is a *foreign key*; in user
     * entities, the user ID is stored as the entity ID.
     */
    public static final Attribute<Long> USER_ID = Attribute.create("user", Long.class);
    /**
     * The item ID associated with an entity. This is for when the user is a *foreign key*; in item
     * entities, the item ID is stored as the entity ID.
     */
    public static final Attribute<Long> ITEM_ID = Attribute.create("item", Long.class);
    /**
     * A name associated with the entity.
     */
    public static final Attribute<String> NAME = Attribute.create("name", String.class);

    /**
     * A timestamp associated with an event entity.
     */
    public static final Attribute<Long> TIMESTAMP = Attribute.create("timestamp", Long.class);
    /**
     * A rating value.
     */
    public static final Attribute<Double> RATING = Attribute.create("rating", Double.class);
    /**
     * A standard count, for events that may use them.
     */
    public static final Attribute<Integer> COUNT = Attribute.create("count", Integer.class);
}
