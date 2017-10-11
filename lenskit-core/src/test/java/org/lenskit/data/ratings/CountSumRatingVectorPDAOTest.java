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
package org.lenskit.data.ratings;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.junit.Test;
import org.lenskit.data.dao.EntityCollectionDAO;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.EntityType;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class CountSumRatingVectorPDAOTest {
    private static final EntityType LIKE = EntityType.forName("LIKE");

    @Test
    public void testNoUser() {
        EntityCollectionDAO dao = EntityCollectionDAO.create();
        RatingVectorPDAO source = new CountSumRatingVectorPDAO(dao, LIKE);

        assertThat(source.userRatingVector(42).entrySet(),
                   hasSize(0));
    }

    @Test
    public void testGetLike() {
        EntityCollectionDAO dao =
                EntityCollectionDAO.create(Entities.newBuilder(LIKE)
                                                   .setId(1)
                                                   .setAttribute(CommonAttributes.USER_ID, 42L)
                                                   .setAttribute(CommonAttributes.ITEM_ID, 39L)
                                                   .setAttribute(CommonAttributes.COUNT, 5)
                                                   .build());
        RatingVectorPDAO source = new CountSumRatingVectorPDAO(dao, LIKE);

        assertThat(source.userRatingVector(42),
                   hasEntry(39L, 5.0));
    }

    @Test
    public void testCountLikes() {
        EntityCollectionDAO dao =
                EntityCollectionDAO.create(Entities.newBuilder(LIKE)
                                                   .setId(1)
                                                   .setAttribute(CommonAttributes.USER_ID, 42L)
                                                   .setAttribute(CommonAttributes.ITEM_ID, 39L)
                                                   .setAttribute(CommonAttributes.COUNT, 5)
                                                   .build(),
                                           Entities.newBuilder(LIKE)
                                                   .setId(2)
                                                   .setAttribute(CommonAttributes.USER_ID, 42L)
                                                   .setAttribute(CommonAttributes.ITEM_ID, 20L)
                                                   .setAttribute(CommonAttributes.COUNT, 2)
                                                   .build(),
                                           Entities.newBuilder(LIKE)
                                                   .setId(3)
                                                   .setAttribute(CommonAttributes.USER_ID, 17L)
                                                   .setAttribute(CommonAttributes.ITEM_ID, 39L)
                                                   .setAttribute(CommonAttributes.COUNT, 7)
                                                   .build(),
                                           Entities.newBuilder(LIKE)
                                                   .setId(3)
                                                   .setAttribute(CommonAttributes.USER_ID, 42L)
                                                   .setAttribute(CommonAttributes.ITEM_ID, 39L)
                                                   .setAttribute(CommonAttributes.COUNT, 1)
                                                   .build());
        RatingVectorPDAO source = new CountSumRatingVectorPDAO(dao, LIKE);

        Long2DoubleMap vec = source.userRatingVector(42);
        assertThat(vec.entrySet(), hasSize(2));
        assertThat(vec, hasEntry(39L, 6.0));
        assertThat(vec, hasEntry(20L, 2.0));
    }
}