/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
/**
 *
 */
package org.grouplens.lenskit.baseline;


import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test baseline predictors that compute means from data.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestMeanPredictor {
    private static final double RATINGS_DAT_MEAN = 3.75;
    private EventCollectionDAO dao;

    @Before
    public void createRatingSource() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 5, 2));
        rs.add(Ratings.make(1, 7, 4));
        rs.add(Ratings.make(8, 4, 5));
        rs.add(Ratings.make(8, 5, 4));

        dao = new EventCollectionDAO(rs);
    }

    LongSortedSet itemSet(long item) {
        return new LongSortedArraySet(new long[]{item});
    }

    @Test
    public void testMeanBaseline() {
        BaselinePredictor pred = new GlobalMeanPredictor.Builder(dao).get();
        SparseVector map = new ImmutableSparseVector(Long2DoubleMaps.EMPTY_MAP);
        SparseVector pv = pred.predict(10L, map, itemSet(2l));
        assertEquals(RATINGS_DAT_MEAN, pv.get(2l), 0.00001);
    }

    @Test
    public void testUserMeanBaseline() {
        BaselinePredictor pred = new UserMeanPredictor.Builder(dao, 0.0).get();
        long[] items = {5, 7, 10};
        double[] ratings = {3, 6, 4};
        SparseVector map = MutableSparseVector.wrap(items, ratings).freeze();
        // unseen item
        SparseVector pv = pred.predict(10L, map, itemSet(2l));
        assertEquals(4.33333, pv.get(2l), 0.001);
        // seen item - should be same avg
        pv = pred.predict(10L, map, itemSet(7));
        assertEquals(4.33333, pv.get(7), 0.001);
    }

    /**
     * Test falling back to an empty user.
     */
    @Test
    public void testUserMeanBaselineFallback() {
        BaselinePredictor pred = new UserMeanPredictor.Builder(dao, 0.0).get();
        SparseVector map = new ImmutableSparseVector(Long2DoubleMaps.EMPTY_MAP);
        SparseVector pv = pred.predict(10l, map, itemSet(2));
        assertEquals(RATINGS_DAT_MEAN, pv.get(2), 0.001);
    }

    @Test
    public void testItemMeanBaseline() {
        BaselinePredictor pred = new ItemMeanPredictor.Builder(dao, 0.0).get();
        long[] items = {5, 7, 10};
        double[] values = {3, 6, 4};
        SparseVector map = MutableSparseVector.wrap(items, values).freeze();
        // unseen item, should be global mean
        SparseVector pv = pred.predict(10l, map, itemSet(2));
        assertEquals(RATINGS_DAT_MEAN, pv.get(2), 0.001);
        // seen item - should be item average
        pv = pred.predict(10l, map, itemSet(5));
        assertEquals(3.0, pv.get(5), 0.001);

        // try twice
        LongCollection items2 = new LongArrayList();
        items2.add(5);
        items2.add(2);
        SparseVector preds = pred.predict(10l, map, items2);
        assertEquals(RATINGS_DAT_MEAN, preds.get(2l), 0.001);
        assertEquals(3.0, preds.get(5l), 0.001);
    }

    @Test
    public void testUserItemMeanBaseline() {
        BaselinePredictor pred = new ItemUserMeanPredictor.Builder(dao, 0.0).get();
        long[] items = {5, 7, 10};
        double[] ratings = {3, 6, 4};
        SparseVector map = MutableSparseVector.wrap(items, ratings).freeze();
        final double avgOffset = 0.75;

        // unseen item, should be global mean + user offset
        SparseVector pv = pred.predict(10l, map, itemSet(2l));
        assertEquals(RATINGS_DAT_MEAN + avgOffset, pv.get(2), 0.001);
        // seen item - should be item average + user offset
        pv = pred.predict(10l, map, itemSet(5));
        assertEquals(3.0 + avgOffset, pv.get(5), 0.001);

        // try twice
        LongCollection items2 = new LongArrayList();
        items2.add(5);
        items2.add(2);
        SparseVector preds = pred.predict(10l, map, items2);
        assertEquals(RATINGS_DAT_MEAN + avgOffset, preds.get(2l), 0.001);
        assertEquals(3.0 + avgOffset, preds.get(5l), 0.001);
    }
}
