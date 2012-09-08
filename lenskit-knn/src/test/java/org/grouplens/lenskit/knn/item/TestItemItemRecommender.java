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
package org.grouplens.lenskit.knn.item;

import static org.grouplens.common.test.MoreMatchers.notANumber;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestItemItemRecommender {

    private LenskitRecommender session;
    private ItemRecommender recommender;

    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 6, 4));
        rs.add(Ratings.make(2, 6, 2));
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
        EventCollectionDAO.Factory manager = new EventCollectionDAO.Factory(rs);
        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(manager);
        factory.bind(ItemScorer.class).to(ItemItemRatingPredictor.class);
        factory.bind(ItemRecommender.class).to(ItemItemRecommender.class);
        // this is the default
        // FIXME Let this work @mludwig
        /*factory.setComponent(UserVectorNormalizer.class, VectorNormalizer.class,
                             IdentityVectorNormalizer.class);*/
        LenskitRecommenderEngine engine = factory.create();
        session = engine.open();
        recommender = session.getItemRecommender();
    }

    /**
     * Check that we score items but do not provide rating scores.
     */
    @Test
    public void testItemScorerNoRating() {
        UserHistory<Rating> history = getRatings(5);
        long[] items = {7, 8};
        ItemItemScorer scorer = session.get(ItemItemScorer.class);
        assertThat(scorer, notNullValue());
        SparseVector scores = scorer.score(history, LongArrayList.wrap(items));
        assertThat(scores, notNullValue());
        assertThat(scores.size(), equalTo(1));
        assertThat(scores.get(7), not(notANumber()));
        assertThat(scores.get(8), notANumber());
        assertThat(scores.containsKey(8), equalTo(false));
    }

    /**
     * Tests {@code recommend(long, SparseVector)}.
     */
    @Test
    public void testItemItemRecommender1() {
        LongList recs = recommender.recommend(getRatings(1));
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(getRatings(2));
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getRatings(3));
        assertEquals(1, recs.size());
        assertTrue(recs.contains(6));

        recs = recommender.recommend(getRatings(4));
        assertEquals(2, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getRatings(5));
        assertEquals(3, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getRatings(6));
        assertEquals(3, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));
        assertTrue(recs.contains(9));
    }

    /**
     * Tests {@code recommend(long, SparseVector, int)}.
     */
    @Test
    public void testItemItemRecommender2() {
        LongList recs = recommender.recommend(getRatings(2), 1);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getRatings(2), 0);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(getRatings(3), 1);
        assertTrue(recs.contains(6) || recs.contains(9));

        recs = recommender.recommend(getRatings(4), 0);
        assertTrue(recs.isEmpty());
    }

    /**
     * Tests {@code recommend(long, SparseVector, Set)}.
     */
    @Test
    public void testItemItemRecommender3() {
        LongList recs = recommender.recommend(getRatings(1), null);
        assertTrue(recs.isEmpty());


        LongOpenHashSet candidates = new LongOpenHashSet();
        candidates.add(6);
        candidates.add(7);
        candidates.add(8);
        candidates.add(9);
        recs = recommender.recommend(getRatings(1), candidates);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(getRatings(2), null);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        candidates.clear();
        candidates.add(7);
        candidates.add(8);
        candidates.add(9);
        recs = recommender.recommend(getRatings(2), candidates);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        candidates.add(6);
        candidates.remove(9);
        recs = recommender.recommend(getRatings(2), candidates);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(getRatings(5), null);
        assertEquals(3, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));
        assertTrue(recs.contains(9));

        candidates.clear();
        candidates.add(6);
        candidates.add(7);
        recs = recommender.recommend(getRatings(5), candidates);
        assertEquals(2, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));

        candidates.clear();
        candidates.add(6);
        candidates.add(9);
        recs = recommender.recommend(getRatings(5), candidates);
        assertEquals(2, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(9));
    }

    /**
     * Tests {@code recommend(long, SparseVector, int, Set, Set)}.
     */
    @Test
    public void testItemItemRecommender4() {
        LongList recs = recommender.recommend(getRatings(5), -1, null, null);
        assertEquals(3, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));
        assertTrue(recs.contains(9));

        LongOpenHashSet candidates = new LongOpenHashSet();
        candidates.add(6);
        candidates.add(7);
        candidates.add(8);
        candidates.add(9);
        recs = recommender.recommend(getRatings(5), -1, candidates, null);
        assertEquals(3, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));
        assertTrue(recs.contains(9));

        candidates.remove(6);
        recs = recommender.recommend(getRatings(5), -1, candidates, null);
        assertEquals(2, recs.size());
        assertTrue(recs.contains(7));
        assertTrue(recs.contains(9));

        candidates.remove(7);
        recs = recommender.recommend(getRatings(5), -1, candidates, null);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        candidates.remove(9);
        recs = recommender.recommend(getRatings(5), -1, candidates, null);
        assertTrue(recs.isEmpty());

        candidates.add(9);
        candidates.add(7);
        recs = recommender.recommend(getRatings(5), 1, candidates, null);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9) || recs.contains(7));

        LongOpenHashSet exclude = new LongOpenHashSet();
        exclude.add(7);
        recs = recommender.recommend(getRatings(5), 2, candidates, exclude);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(getRatings(5), 0, candidates, null);
        assertTrue(recs.isEmpty());

        candidates.clear();
        candidates.add(7);
        candidates.add(9);
        recs = recommender.recommend(getRatings(5), -1, candidates, null);
        assertEquals(2, recs.size());
        assertTrue(recs.contains(7));
        assertTrue(recs.contains(9));

        candidates.add(6);
        exclude.clear();
        exclude.add(9);
        recs = recommender.recommend(getRatings(5), -1, candidates, exclude);
        assertEquals(2, recs.size());
        assertTrue(recs.contains(6));
        assertTrue(recs.contains(7));

        exclude.add(7);
        recs = recommender.recommend(getRatings(5), -1, candidates, exclude);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(6));

        exclude.add(6);
        recs = recommender.recommend(getRatings(5), -1, candidates, exclude);
        assertTrue(recs.isEmpty());
    }

    //Helper method to retrieve user's user and create SparseVector
    private UserHistory<Rating> getRatings(long user) {
        DataAccessObject dao = session.getRatingDataAccessObject();
        return dao.getUserHistory(user, Rating.class);
    }

    @After
    public void cleanUp() {
        session.close();
    }
}
