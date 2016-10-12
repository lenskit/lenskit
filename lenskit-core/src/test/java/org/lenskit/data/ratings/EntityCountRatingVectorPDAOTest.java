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
package org.lenskit.data.ratings;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.junit.Test;
import org.lenskit.data.dao.EntityCollectionDAO;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.EntityType;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class EntityCountRatingVectorPDAOTest {
    private static final EntityType LIKE = EntityType.forName("LIKE");

    @Test
    public void testNoUser() {
        EntityCollectionDAO dao = EntityCollectionDAO.create();
        RatingVectorPDAO source = new EntityCountRatingVectorPDAO(dao, LIKE);

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
                                                   .build());
        RatingVectorPDAO source = new EntityCountRatingVectorPDAO(dao, LIKE);

        assertThat(source.userRatingVector(42),
                   hasEntry(39L, 1.0));
    }

    @Test
    public void testCountLikes() {
        EntityCollectionDAO dao =
                EntityCollectionDAO.create(Entities.newBuilder(LIKE)
                                                   .setId(1)
                                                   .setAttribute(CommonAttributes.USER_ID, 42L)
                                                   .setAttribute(CommonAttributes.ITEM_ID, 39L)
                                                   .build(),
                                           Entities.newBuilder(LIKE)
                                                   .setId(2)
                                                   .setAttribute(CommonAttributes.USER_ID, 42L)
                                                   .setAttribute(CommonAttributes.ITEM_ID, 20L)
                                                   .build(),
                                           Entities.newBuilder(LIKE)
                                                   .setId(3)
                                                   .setAttribute(CommonAttributes.USER_ID, 17L)
                                                   .setAttribute(CommonAttributes.ITEM_ID, 39L)
                                                   .build(),
                                           Entities.newBuilder(LIKE)
                                                   .setId(3)
                                                   .setAttribute(CommonAttributes.USER_ID, 42L)
                                                   .setAttribute(CommonAttributes.ITEM_ID, 39L)
                                                   .build());
        RatingVectorPDAO source = new EntityCountRatingVectorPDAO(dao, LIKE);

        Long2DoubleMap vec = source.userRatingVector(42);
        assertThat(vec.entrySet(), hasSize(2));
        assertThat(vec, hasEntry(39L, 2.0));
        assertThat(vec, hasEntry(20L, 1.0));
    }
}