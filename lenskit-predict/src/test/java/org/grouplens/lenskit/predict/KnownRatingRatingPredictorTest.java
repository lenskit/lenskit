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
package org.grouplens.lenskit.predict;

import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.PrefetchingUserEventDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.RatingBuilder;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.Before;
import org.junit.Test;


import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class KnownRatingRatingPredictorTest {

    private EventDAO dao;
    private UserEventDAO userDAO;
    private List<Rating> rs = new ArrayList<Rating>();

    @SuppressWarnings("deprecation")
    @Before
    //Setup method
    public void createPredictor() throws RecommenderBuildException {
        rs.add(Ratings.make(14, 1, 5));
        rs.add(Ratings.make(14, 2, 4));
        rs.add(Ratings.make(14, 3, 3));
        rs.add(Ratings.make(14, 4, 2));
        rs.add(Ratings.make(14, 5, 0.2));
        rs.add(Ratings.make(15, 5, 1));
        rs.add(Ratings.make(15, 6, 2));
        rs.add(Ratings.make(15, 7, 3));
        rs.add(Ratings.make(15, 8, 4));
        rs.add(Ratings.make(15, 9, 5));

        dao = new EventCollectionDAO(rs);
        userDAO = new PrefetchingUserEventDAO(dao);
    }

    @Test
    /*
    * Test method that tests predicting for a user not in the data set
    * */
    public void testPredictForMissingUser() {
        KnownRatingRatingPredictor KnownPredict = new KnownRatingRatingPredictor(userDAO);
        MutableSparseVector predictItems = MutableSparseVector.create(1, 2);
        KnownPredict.predict(5, predictItems);
        assertThat(predictItems.size(), equalTo(0));
    }

    @Test
     /*
    * Test method that tests predicting for a user in the data set,
    * only with items they have rated.
    * */
    public void testPredictForRatingByGivenUser() {
        KnownRatingRatingPredictor KnownPredict = new KnownRatingRatingPredictor(userDAO);
        long[] keys= {1,3,5};
        double[] values= {1.0,2.0,4.0};
        MutableSparseVector predictItems = MutableSparseVector.wrap(keys, values);
        KnownPredict.predict(14, predictItems);
        assertThat(predictItems.get(1), equalTo(5.0));
        assertThat(predictItems.get(3), equalTo(3.0));
        assertThat(predictItems.get(5), equalTo(0.2));
    }

    @Test
     /*
    * Test method that tests predicting for a user in the data set,
    * returning the rating of item for given user only.
    * */
    public void  testPredictForOnlyRatedItems() {
        KnownRatingRatingPredictor KnownPredict = new KnownRatingRatingPredictor(userDAO);
        MutableSparseVector predictItems = MutableSparseVector.create(5, 7, 1);
        KnownPredict.predict(15, predictItems);
        assertThat(predictItems.get(5), equalTo(1.0));
        assertThat(predictItems.get(7), equalTo(3.0));
        assertFalse(predictItems.containsKey(1));

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

        KnownRatingRatingPredictor KnownPredict = new KnownRatingRatingPredictor(userDAO);
        MutableSparseVector predictItems = MutableSparseVector.create(840,390);
        KnownPredict.predict(420, predictItems);
        assertThat(predictItems.get(390), equalTo(4.5));
        assertFalse(predictItems.containsKey(840));

    }
}