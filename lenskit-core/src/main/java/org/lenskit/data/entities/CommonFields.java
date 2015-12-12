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
public final class CommonFields {
    private CommonFields() {}

    /**
     * The user ID associated with an entity.
     */
    public static final Field<Long> USER_ID = Field.create("user", Long.class);
    /**
     * The user ID associated with an entity.
     */
    public static final Field<Long> ITEM_ID = Field.create("item", Long.class);
    /**
     * A timestamp associated with an event entity.
     */
    public static final Field<Long> TIMESTAMP = Field.create("timestamp", Long.class);
    /**
     * A rating value.
     */
    public static final Field<Double> RATING = Field.create("rating", Double.class);
    /**
     * A standard count, for events that may use them.
     */
    public static final Field<Integer> COUNT = Field.create("count", Integer.class);
}
