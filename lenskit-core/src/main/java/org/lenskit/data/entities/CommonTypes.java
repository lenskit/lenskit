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
