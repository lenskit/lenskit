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
package org.lenskit.predict;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingBuilder;
import org.lenskit.util.collections.LongUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class KnownRatingRatingPredictorTest {

    private DataAccessObject dao;
    private List<Rating> rs = new ArrayList<>();

    @SuppressWarnings("deprecation")
    @Before
    public void createPredictor() throws RecommenderBuildException {
        rs.add(Rating.create(14, 1, 5));
        rs.add(Rating.create(14, 2, 4));
        rs.add(Rating.create(14, 3, 3));
        rs.add(Rating.create(14, 4, 2));
        rs.add(Rating.create(14, 5, 0.2));
        rs.add(Rating.create(15, 5, 1));
        rs.add(Rating.create(15, 6, 2));
        rs.add(Rating.create(15, 7, 3));
        rs.add(Rating.create(15, 8, 4));
        rs.add(Rating.create(15, 9, 5));

        StaticDataSource src = new StaticDataSource();
        src.addSource(rs);
        dao = src.get();
    }

    /**
     * Test method that tests predicting for a user not in the data set
     */
    @Test
    public void testPredictForMissingUser() {
        KnownRatingRatingPredictor pred = new KnownRatingRatingPredictor(dao);
        Map<Long,Double> results = pred.predict(5, LongUtils.packedSet(1L, 2L));
        assertThat(results.size(), equalTo(0));
    }

    /**
     * Test method that tests predicting for a user in the data set,
     * only with items they have rated.
     */
    @Test
    public void testPredictForRatingByGivenUser() {
        KnownRatingRatingPredictor pred = new KnownRatingRatingPredictor(dao);
        Map<Long,Double> results = pred.predict(14, LongUtils.packedSet(1, 3, 5));
        assertThat(results.size(), equalTo(3));
        assertThat(results.get(1L), equalTo(5.0));
        assertThat(results.get(3L), equalTo(3.0));
        assertThat(results.get(5L), equalTo(0.2));
    }

    /**
     * Test method that tests predicting for a user in the data set,
     * returning the rating of item for given user only.
     */
    @Test
    public void  testPredictForOnlyRatedItems() {
        KnownRatingRatingPredictor pred = new KnownRatingRatingPredictor(dao);
        Map<Long,Double> results = pred.predict(15, LongUtils.packedSet(5, 7, 1));
        assertThat(results.get(5L), equalTo(1.0));
        assertThat(results.get(7L), equalTo(3.0));
        assertThat(results.keySet(),
                   not(hasItem(1L)));

    }
    @SuppressWarnings("deprecation")
    @Test
     /*
    * Test method that tests unrated items for a user in the data set,
    * it shouldn't return any value.
    * */
    @Ignore("unrates are going away")
    public void  testPredictForUnratedItems() {
        RatingBuilder rb = new RatingBuilder().setUserId(420);
        rs.add(rb.setItemId(840).setRating(3.5).setTimestamp(10).build());
        rs.add(rb.setItemId(390).setRating(4.5).setTimestamp(20).build());
        rs.add(rb.setItemId(840).clearRating().setTimestamp(30).build());

        dao = StaticDataSource.fromList(rs).get();

        KnownRatingRatingPredictor pred = new KnownRatingRatingPredictor(dao);
        Map<Long,Double> results = pred.predict(420, LongUtils.packedSet(840, 390));
        assertThat(results, hasEntry(390L, 4.5));
        assertThat(results.keySet(),
                   not(Matchers.hasItem(840L)));
    }
}
