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