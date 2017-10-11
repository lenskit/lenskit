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

import org.junit.Test;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.EntityCollectionDAO;
import org.lenskit.data.dao.EntityCollectionDAOBuilder;
import org.lenskit.data.entities.EntityFactory;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class RatingSummaryTest {
    @Test
    public void testEmptySummary() {
        DataAccessObject dao = EntityCollectionDAO.create();
        RatingSummary sum = RatingSummary.create(dao);
        assertThat(sum.getGlobalMean(), equalTo(0.0));
        assertThat(sum.getItemMean(42), notANumber());
        assertThat(sum.getItemOffset(42), equalTo(0.0));
        assertThat(sum.getItemRatingCount(42), equalTo(0));
    }

    @Test
    public void testSummaryItem() {
        EntityFactory efac = new EntityFactory();
        EntityCollectionDAOBuilder daoB = new EntityCollectionDAOBuilder();
        // add ratings at 3.9 and 3.1, to make 3.5 average
        for (int i = 0; i < 100; i++) {
            daoB.addEntities(efac.rating(i, 37L, 3.9 + (i - 49.5) * 0.01));
            daoB.addEntities(efac.rating(i, 82L, 3.1 + (i - 49.5) * 0.01));
        }
        RatingSummary sum = RatingSummary.create(daoB.build());

        assertThat(sum.getGlobalMean(), equalTo(3.5));

        assertThat(sum.getItemMean(42), notANumber());
        assertThat(sum.getItemOffset(42), equalTo(0.0));
        assertThat(sum.getItemRatingCount(42), equalTo(0));

        assertThat(sum.getItemMean(37), equalTo(3.9));
        assertThat(sum.getItemOffset(37), closeTo(0.4, 1.0e-6));
        assertThat(sum.getItemRatingCount(37), equalTo(100));

        assertThat(sum.getItemMean(82), equalTo(3.1));
        assertThat(sum.getItemOffset(82), closeTo(-0.4, 1.0e-6));
        assertThat(sum.getItemRatingCount(82), equalTo(100));
    }
}
