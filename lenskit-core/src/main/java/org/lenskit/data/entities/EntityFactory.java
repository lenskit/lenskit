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

import org.lenskit.data.ratings.Rating;

/**
 * Class for quickly building common entity types.  This class automatically assigns entity IDs and makes it quick
 * and easy to build entities.
 */
public class EntityFactory {
    private long entityId = 0;

    public EntityFactory() {

    }

    public Rating rating(long uid, long iid, double rating) {
        return Rating.newBuilder()
                     .setId(++entityId)
                     .setUserId(uid)
                     .setItemId(iid)
                     .setRating(rating)
                     .build();
    }

    public Rating rating(long uid, long iid, double rating, long timestamp) {
        return Rating.newBuilder()
                     .setId(++entityId)
                     .setUserId(uid)
                     .setItemId(iid)
                     .setRating(rating)
                     .setTimestamp(timestamp)
                     .build();
    }

    public Entity likeBatch(long uid, long iid, int count) {
        return Entities.newBuilder(EntityType.forName("like-batch"))
                       .setId(++entityId)
                       .setLongAttribute(CommonAttributes.USER_ID, uid)
                       .setLongAttribute(CommonAttributes.ITEM_ID, iid)
                       .setAttribute(CommonAttributes.COUNT, count)
                       .build();
    }
}
