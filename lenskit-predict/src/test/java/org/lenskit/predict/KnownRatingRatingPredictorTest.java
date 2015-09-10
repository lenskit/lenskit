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
package org.lenskit.predict;

import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.PrefetchingUserEventDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingBuilder;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class KnownRatingRatingPredictorTest {

    private EventDAO dao;
    private UserEventDAO userDAO;
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

        dao = new EventCollectionDAO(rs);
        userDAO = new PrefetchingUserEventDAO(dao);
    }

    /**
     * Test method that tests predicting for a user not in the data set
     */
    @Test
    public void testPredictForMissingUser() {
        KnownRatingRatingPredictor pred = new KnownRatingRatingPredictor(userDAO);
        Map<Long,Double> results = pred.predict(5, LongUtils.packedSet(1L, 2L));
        assertThat(results.size(), equalTo(0));
    }

    /**
     * Test method that tests predicting for a user in the data set,
     * only with items they have rated.
     */
    @Test
    public void testPredictForRatingByGivenUser() {
        KnownRatingRatingPredictor pred = new KnownRatingRatingPredictor(userDAO);
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
        KnownRatingRatingPredictor pred = new KnownRatingRatingPredictor(userDAO);
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
    public void  testPredictForUnratedItems() {
        RatingBuilder rb = new RatingBuilder().setUserId(420);
        rs.add(rb.setItemId(840).setRating(3.5).setTimestamp(10).build());
        rs.add(rb.setItemId(390).setRating(4.5).setTimestamp(20).build());
        rs.add(rb.setItemId(840).clearRating().setTimestamp(30).build());

        dao = new EventCollectionDAO(rs);
        userDAO = new PrefetchingUserEventDAO(dao);

        KnownRatingRatingPredictor pred = new KnownRatingRatingPredictor(userDAO);
        Map<Long,Double> results = pred.predict(420, LongUtils.packedSet(840, 390));
        assertThat(results, hasEntry(390L, 4.5));
        assertThat(results.keySet(),
                   not(Matchers.hasItem(840L)));
    }
}
