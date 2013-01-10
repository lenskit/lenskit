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
package org.grouplens.lenskit.mf.funksvd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.*;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.UserMeanPredictor;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.snapshot.PackedPreferenceSnapshot;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.mf.funksvd.params.FeatureCount;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Unstable based on parameters")
public class TestFunkSVDRecommender {

    private static Recommender svdRecommender;
    private static ItemRecommender recommender;
    private static DataAccessObject dao;

    @BeforeClass
    public static void setup() throws RecommenderBuildException {
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
        factory.bind(PreferenceSnapshot.class).to(PackedPreferenceSnapshot.class);
        factory.bind(RatingPredictor.class).to(FunkSVDRatingPredictor.class);
        factory.bind(BaselinePredictor.class).to(UserMeanPredictor.class);
        factory.bind(ItemRecommender.class).to(FunkSVDRecommender.class);
        factory.bind(Integer.class).withQualifier(FeatureCount.class).to(100);
        // FIXME: Don't use 100 features.
        RecommenderEngine engine = factory.create();
        svdRecommender = engine.open();
        recommender = svdRecommender.getItemRecommender();
        dao = manager.create();
    }


    /**
     * Tests {@code recommend(long)}.
     */
    @Test
    public void testRecommend1() {

        LongList recs = recommender.recommend(1);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(2);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(9));

        recs = recommender.recommend(3);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(6));

        recs = recommender.recommend(4);
        assertEquals(2, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(9, recs.getLong(1));

        recs = recommender.recommend(5);
        assertEquals(3, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(9, recs.getLong(2));

        recs = recommender.recommend(6);
        assertEquals(3, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(9, recs.getLong(2));
    }

    /**
     * Tests {@code recommend(long, int)}.
     */
    @Test
    public void testRecommend2() {

        LongList recs = recommender.recommend(6, 4);
        assertEquals(3, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(9, recs.getLong(2));

        recs = recommender.recommend(6, 3);
        assertEquals(3, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(9, recs.getLong(2));

        recs = recommender.recommend(6, 2);
        assertEquals(2, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(7, recs.getLong(1));

        recs = recommender.recommend(6, 1);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(6));

        recs = recommender.recommend(6, 0);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(6, -1);
        assertEquals(3, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(9, recs.getLong(2));
    }

    /**
     * Tests {@code recommend(long, Set)}.
     */
    @Test
    public void testRecommend3() {

        LongList recs = recommender.recommend(5, null);
        assertEquals(3, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(9, recs.getLong(2));

        LongOpenHashSet candidates = new LongOpenHashSet();
        candidates.add(6);
        candidates.add(7);
        candidates.add(8);
        candidates.add(9);
        recs = recommender.recommend(5, candidates);
        assertEquals(3, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(9, recs.getLong(2));

        candidates.remove(8);
        recs = recommender.recommend(5, candidates);
        assertEquals(3, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(9, recs.getLong(2));

        candidates.remove(7);
        recs = recommender.recommend(5, candidates);
        assertEquals(2, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(9, recs.getLong(1));

        candidates.remove(6);
        recs = recommender.recommend(5, candidates);
        assertEquals(1, recs.size());
        assertEquals(9, recs.getLong(0));

        candidates.remove(9);
        recs = recommender.recommend(5, candidates);
        assertTrue(recs.isEmpty());

        candidates.add(8);
        recs = recommender.recommend(5, candidates);
        assertTrue(recs.isEmpty());
    }

    /**
     * Tests {@code recommend(long, int, Set, Set)}.
     */
    @Test
    public void testRecommend4() {
        LongList recs = recommender.recommend(6, -1, null, null);
        assertEquals(4, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(9, recs.getLong(2));
        assertEquals(8, recs.getLong(3));

        LongOpenHashSet exclude = new LongOpenHashSet();
        exclude.add(9);
        recs = recommender.recommend(6, -1, null, exclude);
        assertEquals(3, recs.size());
        assertEquals(6, recs.getLong(0));
        assertEquals(7, recs.getLong(1));
        assertEquals(8, recs.getLong(2));

        exclude.add(6);
        recs = recommender.recommend(6, -1, null, exclude);
        assertEquals(2, recs.size());
        assertEquals(7, recs.getLong(0));
        assertEquals(8, recs.getLong(1));

        exclude.add(8);
        recs = recommender.recommend(6, -1, null, exclude);
        assertEquals(1, recs.size());
        assertTrue(recs.contains(7));
    }

    @AfterClass
    public static void cleanUp() {
        svdRecommender.close();
        dao.close();
    }
}
