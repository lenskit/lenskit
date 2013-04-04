/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package ${package};

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.closeTo;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test baseline Scorers that compute means from data.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class TestMeanScorer {
    private DataAccessObject dao;

    @Before
    public void createRatingSource() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(new SimpleRating(1, 1, 5, 3));
        rs.add(new SimpleRating(2, 1, 7, 4));
        rs.add(new SimpleRating(3, 8, 4, 5));
        rs.add(new SimpleRating(4, 8, 5, 4));
        
        // Global Mean: 16 / 4 = 4

        // Item  Means  Offsets
        // 5 ->   3.5     -0.5 
        // 7 ->   4.0      0.0
        // 4 ->   5.0      1.0

        // User  Offset Avg
        //  1      -0.5+0.0 / 2 = -0.25
        //  8      0.0+0.5 / 2  = 0.25

        // Preds
        // u1 on i5 -> 3.25
        // u1 on i7 -> 3.75
        // u1 on i10 -> unable to predict
        // u1 on i4 -> 4.75
        // u8 on i5 -> 3.75
        // u8 on i7 -> 4.25
        // u8 on i4 -> 5.25 (?)
        // u2 on i4 -> 5.0
        // u2 on i7 -> 4.0
        // u2 on i5 -> 3.5
        dao = new EventCollectionDAO.Factory(rs).create();
    }

    @After
    public void closeRatingSession() {
        dao.close();
    }

    LongSortedSet itemSet(long item) {
        return new LongSortedArraySet(new long[]{item});
    }

    @Test
    public void testUserItemMeanScorer() {
        ItemScorer scorer = new ExtendedItemUserMeanScorer(dao, new ItemMeanModel.Provider(dao, 0).get(), 0);

        long[] items = {5, 7, 10};
        double[] ratings = {3, 6, 4};

        // User 1
        MutableSparseVector scores1 = MutableSparseVector.wrap(items, ratings); // ratings ignored
        scorer.score(1L, scores1);
        assertThat(scores1.get(5), closeTo(3.25, 1.0e-5));
        assertThat(scores1.get(7), closeTo(3.75, 1.0e-5));
        assertThat(scores1.get(10), closeTo(3.75, 1.0e-5));  // user overall average
        assertTrue(Double.isNaN(scores1.get(4)));

        // User 8
        long[] items8 = {4, 5, 7};

        MutableSparseVector scores8 = MutableSparseVector.wrap(items8, ratings); // ratings ignored
        scorer.score(8L, scores8);
        assertThat(scores8.get(5), closeTo(3.75, 1.0e-5));
        assertThat(scores8.get(7), closeTo(4.25, 1.0e-5));
        assertThat(scores8.get(4), closeTo(5.25, 1.0e-5));
        
        // User 2, not in the set of users in the DAO
        MutableSparseVector scores2 = MutableSparseVector.wrap(items, ratings); // ratings ignored
        scorer.score(2L, scores2);
        assertThat(scores2.get(5), closeTo(3.5, 1.0e-5));
        assertTrue(Double.isNaN(scores2.get(4)));
        assertThat(scores2.get(7), closeTo(4, 1.0e-5));
     }
    
    @Test
    public void testUserItemMeanMissing() {
        ItemScorer scorer = new ExtendedItemUserMeanScorer(dao, new ItemMeanModel.Provider(dao, 0).get(), 0);

        long[] items = {5, 7, 10};
        double[] ratings = {3, 6, 4};

        // User 1
        MutableSparseVector scores1 = MutableSparseVector.wrap(items, ratings); // ratings ignored
        scores1.clear(5L);  // make sure scores are returned even if the ratings are not set
        scorer.score(1L, scores1);
        assertThat(scores1.get(5), closeTo(3.25, 1.0e-5));
        assertThat(scores1.get(7), closeTo(3.75, 1.0e-5));
        assertThat(scores1.get(10), closeTo(3.75, 1.0e-5));  // user overall average
        assertTrue(Double.isNaN(scores1.get(4)));
     }
}
