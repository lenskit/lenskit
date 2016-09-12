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
 * Definitions and utiltiies for common entity types.
 */
public class CommonTypes {
    /**
     * Type identifier for *user* entities.  Users may have {@link CommonAttributes#NAME} attributes, along with any
     * other custom attributes defined by the application.
     */
    public static final EntityType USER = EntityType.forName("user");
    /**
     * Type identifier for *item* entities.  Items may have {@link CommonAttributes#NAME} attributes, along with any
     * other custom attributes defined by the application.
     */
    public static final EntityType ITEM = EntityType.forName("item");
    /**
     * Type identifier for *rating* entities.
     *
     * Each rating represents the user's expressed preference for an item.  Ratings are guaranteed to have
     * {@link CommonAttributes#USER_ID}, {@link CommonAttributes#ITEM_ID}, and {@link CommonAttributes#RATING}
     * attributes, and may have a {@link CommonAttributes#TIMESTAMP}.  They are generally accessed via the
     * {@link org.lenskit.data.ratings.Rating} interface, unless access to additional fields is required.
     *
     * Ratings should reflect the user's current stated preference.  This is a change from LensKit 2.x, where
     * ratings are events; in LensKit 3.0, applications must update and remove rating entities to reflect the
     * user's actions.  If actual rating application or removal events are required the application should define
     * separate entity type(s) to describe these actions.
     */
    public static final EntityType RATING = EntityType.forName("rating");
}
