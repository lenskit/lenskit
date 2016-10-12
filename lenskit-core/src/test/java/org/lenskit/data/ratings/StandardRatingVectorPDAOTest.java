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
import org.lenskit.data.entities.EntityFactory;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class StandardRatingVectorPDAOTest {
    EntityFactory factory = new EntityFactory();

    @Test
    public void testNoUser() {
        EntityCollectionDAO dao = EntityCollectionDAO.create();
        RatingVectorPDAO source = new StandardRatingVectorPDAO(dao);

        assertThat(source.userRatingVector(42).entrySet(),
                   hasSize(0));
    }

    @Test
    public void testGetRating() {
        EntityCollectionDAO dao = EntityCollectionDAO.create(factory.rating(42, 39, 2.5));
        RatingVectorPDAO source = new StandardRatingVectorPDAO(dao);

        assertThat(source.userRatingVector(42),
                   hasEntry(39L, 2.5));
    }

    @Test
    public void testGetsOnlyRating() {
        EntityCollectionDAO dao = EntityCollectionDAO.create(factory.rating(42, 39, 2.5),
                                                             factory.rating(42, 20, 3.5),
                                                             factory.rating(17, 39, 1.5));
        RatingVectorPDAO source = new StandardRatingVectorPDAO(dao);

        Long2DoubleMap vec = source.userRatingVector(42);
        assertThat(vec.entrySet(), hasSize(2));
        assertThat(vec, hasEntry(39L, 2.5));
        assertThat(vec, hasEntry(20L, 3.5));
    }
}