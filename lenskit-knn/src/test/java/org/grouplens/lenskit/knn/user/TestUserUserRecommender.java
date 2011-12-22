/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.knn.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSets;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestUserUserRecommender {
    private Recommender rec;
    private EventCollectionDAO dao;

    @Before
    public void setup() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 6, 4));
        rs.add(Ratings.make(2, 6, 2));
        rs.add(Ratings.make(4, 6, 3));
        rs.add(Ratings.make(5, 6, 4));
        rs.add(Ratings.make(1, 7, 3));
        rs.add(Ratings.make(2, 7, 2));
        rs.add(Ratings.make(3, 7, 5));
        rs.add(Ratings.make(4, 7, 2));
        rs.add(Ratings.make(1, 8, 3));
        rs.add(Ratings.make(2, 8, 4));
        rs.add(Ratings.make(3, 8, 3));
        rs.add(Ratings.make(4, 8, 2));
        rs.add(Ratings.make(5, 8, 3));
        rs.add(Ratings.make(6, 8, 2));
        rs.add(Ratings.make(1, 9, 3));
        rs.add(Ratings.make(3, 9, 4));
        rs.add(Ratings.make(6, 9, 4));
        rs.add(Ratings.make(5, 9, 4));
        EventCollectionDAO.Factory manager = new EventCollectionDAO.Factory(rs);
        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(manager);
        factory.setComponent(RatingPredictor.class, UserUserRatingPredictor.class);
        factory.setComponent(ItemRecommender.class, UserUserRecommender.class);
        factory.setComponent(NeighborhoodFinder.class, SimpleNeighborhoodFinder.class);
        // this is the default
/*        factory.setComponent(UserVectorNormalizer.class,
                             VectorNormalizer.class,
                             IdentityVectorNormalizer.class);*/
        RecommenderEngine engine = factory.create();
        rec = engine.open();
        dao = manager.create();
    }

    /**
     * Tests <tt>recommend(long, SparseVector)</tt>.
     */
    @Test
    public void testUserUserRecommender1() {
        ItemRecommender recommender = rec.getItemRecommender();
        LongList recs = recommender.recommend(getUserRatings(1));
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(getUserRatings(2));
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getUserRatings(3));
        assertEquals(1, recs.size());
        assertTrue(recs.contains(6));

        recs = recommender.recommend(getUserRatings(4));
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getUserRatings(5));
        assertEquals(1, recs.size());
        assertTrue(recs.contains(7));

        recs = recommender.recommend(getUserRatings(6));
        assertEquals(2, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));
    }

    /**
     * Tests <tt>recommend(long, SparseVector, int)</tt>.
     */
    @Test
    public void testUserUserRecommender2() {
        ItemRecommender recommender = rec.getItemRecommender();
        LongList recs = recommender.recommend(getUserRatings(1), -1);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(getUserRatings(2), 2);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getUserRatings(2), -1);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getUserRatings(2), 1);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getUserRatings(2), 0);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(getUserRatings(3), 1);
        assertEquals(1, recs.size());
        assertEquals(6, recs.getLong(0));

        recs = recommender.recommend(getUserRatings(3), 0);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(getUserRatings(4), 1);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getUserRatings(5), -1);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(7));

        recs = recommender.recommend(getUserRatings(6), 2);
        assertEquals(2, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));

        recs = recommender.recommend(getUserRatings(6), 1);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(6) || recs.contains(7));

        recs = recommender.recommend(getUserRatings(6), 0);
        assertTrue(recs.isEmpty());
    }

    /**
     * Tests <tt>recommend(long, SparseVector, Set)</tt>.
     */
    @Test
    public void testUserUserRecommender3() {
        ItemRecommender recommender = rec.getItemRecommender();

        LongOpenHashSet candidates = new LongOpenHashSet();
        candidates.add(6);
        candidates.add(7);
        candidates.add(8);
        candidates.add(9);

        LongList recs = recommender.recommend(getUserRatings(1), candidates);
        assertTrue(recs.isEmpty());

        candidates.clear();
        candidates.add(9);
        recs = recommender.recommend(getUserRatings(2), candidates);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        candidates.clear();
        candidates.add(6);
        candidates.add(7);
        candidates.add(8);
        recs = recommender.recommend(getUserRatings(2), candidates);
        assertTrue(recs.isEmpty());

        candidates.add(9);
        recs = recommender.recommend(getUserRatings(3), candidates);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(6));

        recs = recommender.recommend(getUserRatings(4), candidates);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getUserRatings(5), candidates);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(7));

        candidates.remove(7);
        recs = recommender.recommend(getUserRatings(5), candidates);
        assertTrue(recs.isEmpty());

        candidates.add(7);
        recs = recommender.recommend(getUserRatings(6), candidates);
        assertEquals(2, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));

        candidates.remove(9);
        recs = recommender.recommend(getUserRatings(6), candidates);
        assertEquals(2, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));

        candidates.remove(8);
        recs = recommender.recommend(getUserRatings(6), candidates);
        assertEquals(2, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));

        candidates.remove(7);
        recs = recommender.recommend(getUserRatings(6), candidates);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(6));

        candidates.remove(6);
        recs = recommender.recommend(getUserRatings(6), candidates);
        assertTrue(recs.isEmpty());
    }

    /**
     * Tests <tt>recommend(long, SparseVector, int, Set, Set)</tt>.
     */
    @Test
    public void testUserUserRecommender4() {
        ItemRecommender recommender = rec.getItemRecommender();

        LongOpenHashSet candidates = new LongOpenHashSet();
        candidates.add(9);
        LongList recs = recommender.recommend(getUserRatings(2), -1, candidates, null);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getUserRatings(2), 1, candidates, null);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getUserRatings(2), 0, candidates, null);
        assertTrue(recs.isEmpty());

        LongOpenHashSet exclude = new LongOpenHashSet();
        exclude.add(9);
        recs = recommender.recommend(getUserRatings(2), -1, candidates, exclude);
        assertTrue(recs.isEmpty());

        // FIXME Add tests for default exclude set
        recs = recommender.recommend(getUserRatings(5), -1, null, LongSets.EMPTY_SET);
        assertEquals(4, recs.size());
        assertEquals(9, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(6, recs.getLong(2));
        assertEquals(8, recs.getLong(3));

        recs = recommender.recommend(getUserRatings(5), 5, null, LongSets.EMPTY_SET);
        assertEquals(4, recs.size());
        assertEquals(9, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(6, recs.getLong(2));
        assertEquals(8, recs.getLong(3));

        recs = recommender.recommend(getUserRatings(5), 4, null, LongSets.EMPTY_SET);
        assertEquals(4, recs.size());
        assertEquals(9, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(6, recs.getLong(2));
        assertEquals(8, recs.getLong(3));

        recs = recommender.recommend(getUserRatings(5), 3, null, LongSets.EMPTY_SET);
        assertEquals(3, recs.size());
        assertEquals(9, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(6,recs.getLong(2));

        recs = recommender.recommend(getUserRatings(5), 2, null, LongSets.EMPTY_SET);
        assertEquals(2, recs.size());
        assertEquals(9, recs.getLong(0));
        assertEquals(7, recs.getLong(1));

        recs = recommender.recommend(getUserRatings(5), 1, null, LongSets.EMPTY_SET);
        assertEquals(1, recs.size());
        assertEquals(9, recs.getLong(0));

        recs = recommender.recommend(getUserRatings(5), 0, null, LongSets.EMPTY_SET);
        assertTrue(recs.isEmpty());

        candidates.clear();
        candidates.add(6);
        candidates.add(7);
        recs = recommender.recommend(getUserRatings(6), -1, candidates, LongSets.EMPTY_SET);
        assertEquals(2, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));

        candidates.remove(6);
        recs = recommender.recommend(getUserRatings(6), -1, candidates, LongSets.EMPTY_SET);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(7));

        candidates.remove(7);
        recs = recommender.recommend(getUserRatings(6), -1, candidates, LongSets.EMPTY_SET);
        assertTrue(recs.isEmpty());

        candidates.add(6);
        candidates.add(7);
        exclude.add(6);
        recs = recommender.recommend(getUserRatings(6), -1, candidates, exclude);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(7));

        exclude.add(7);
        recs = recommender.recommend(getUserRatings(6), -1, candidates, exclude);
        assertTrue(recs.isEmpty());

        exclude.remove(6);
        recs = recommender.recommend(getUserRatings(6), -1, candidates, exclude);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(6));

    }

    //Helper method to retrieve user's user and create SparseVector
    private UserHistory<Rating> getUserRatings(long user) {
        return dao.getUserHistory(user, Rating.class);
    }

    @After
    public void cleanUp() {
        rec.close();
        dao.close();
    }
}
