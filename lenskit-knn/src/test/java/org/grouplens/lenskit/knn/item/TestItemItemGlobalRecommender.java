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
package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.grouplens.lenskit.GlobalItemRecommender;
import org.grouplens.lenskit.GlobalItemScorer;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.transform.normalize.DefaultUserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.IdentityVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.VectorNormalizer;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.grouplens.common.test.MoreMatchers.notANumber;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

public class TestItemItemGlobalRecommender {
    private LenskitRecommender session;
    private GlobalItemRecommender gRecommender;

    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 1, 1));
        rs.add(Ratings.make(1, 5, 1));
        rs.add(Ratings.make(2, 1, 1));
        rs.add(Ratings.make(2, 7, 1));
        rs.add(Ratings.make(3, 7, 1));
        rs.add(Ratings.make(4, 1, 1));
        rs.add(Ratings.make(4, 5, 1));
        rs.add(Ratings.make(4, 7, 1));
        rs.add(Ratings.make(4, 10, 1));
        EventCollectionDAO.Factory manager = new EventCollectionDAO.Factory(rs);
        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(manager);
        factory.bind(GlobalItemRecommender.class).to(ItemItemGlobalRecommender.class);
        factory.bind(GlobalItemScorer.class).to(ItemItemGlobalScorer.class);
        // this is the default
        factory.bind(UserVectorNormalizer.class)
                .to(DefaultUserVectorNormalizer.class);
        factory.bind(VectorNormalizer.class)
               .to(IdentityVectorNormalizer.class);
        LenskitRecommenderEngine engine = factory.create();
        session = engine.open();
        gRecommender = session.getGlobalItemRecommender();
    }

    /**
     * Check that we score items but do not provide scores for items
     * the user has previously rated.
     */
    @Test
    public void testGlobalItemScorerNoRating() {
        long[] queryItems = {1, 10};
        long[] items = {5, 10};
        ItemItemGlobalScorer scorer = session.get(ItemItemGlobalScorer.class);
        assertThat(scorer, notNullValue());
        SparseVector scores = scorer.globalScore(LongArrayList.wrap(queryItems), LongArrayList.wrap(items));
        assertThat(scores, notNullValue());
        assertThat(scores.size(), equalTo(2));
        assertThat(scores.get(5), not(notANumber()));
        // assertThat(scores.get(10), equalTo(0.0));

    }

    /**
     * Tests {@code globalRecommend(long)}.
     */
    // FIXME Give the test methods for global item-item meaningful names
    @Test
    public void testGlobalItemItemRecommender1() {
        LongList recs = gRecommender.globalRecommend(LongSets.singleton(1));
        assertThat(recs.size(), notNullValue());
        recs = gRecommender.globalRecommend(LongSets.singleton(2));
        assertThat(recs, empty());
        recs = gRecommender.globalRecommend(LongSets.singleton(5));
        assertThat(recs.size(), notNullValue());
        recs = gRecommender.globalRecommend(LongSets.singleton(1));
        assertThat(recs.size(), notNullValue());
        recs = gRecommender.globalRecommend(LongSets.singleton(10));
        assertThat(recs.size(), notNullValue());

    }

    /**
     * Tests {@code globalRecommend(long, int)}.
     */
    @Test
    public void testGlobalItemItemRecommender2() {
        LongList recs = gRecommender.globalRecommend(LongSets.singleton(1), 2);
        assertThat(recs, hasSize(2));
        recs = gRecommender.globalRecommend(LongSets.singleton(2), 1);
        assertThat(recs, empty());
        recs = gRecommender.globalRecommend(LongSets.singleton(5), 3);
        assertThat(recs, hasSize(3));
    }

    /**
     * Tests {@code globalRecommend(long, Set)}.
     */
    @Test
    public void testGlobalItemItemRecommender3() {
        HashSet<Long> candidates = new HashSet<Long>();
        LongList recs = gRecommender.globalRecommend(LongSets.singleton(1), candidates);
        assertThat(recs, hasSize(0));
        candidates.add(1L);
        candidates.add(5L);
        recs = gRecommender.globalRecommend(LongSets.singleton(1), candidates);
        assertThat(recs, hasSize(1));
        assertTrue(recs.contains(5));

    }

    /**
     * Tests {@code globalRecommend(long, int, Set, Set)}.
     */
    @Test
    public void testGlobalItemItemRecommender4() {
        HashSet<Long> candidates = new HashSet<Long>();
        HashSet<Long> excludes = new HashSet<Long>();
        LongList recs = gRecommender.globalRecommend(LongSets.singleton(1), 1, candidates, excludes);
        assertThat(recs, hasSize(0));
        candidates.add(1L);
        candidates.add(5L);
        excludes.add(5L);
        recs = gRecommender.globalRecommend(LongSets.singleton(1), 2, candidates, excludes);
        assertThat(recs, hasSize(1));
        recs = gRecommender.globalRecommend(LongSets.singleton(1), -1, candidates, excludes);
        assertThat(recs, hasSize(1));

    }

    /**
     * Tests {@code globalRecommend(Set, int)}.
     */
    @Test
    public void testGlobalItemItemRecommender5() {
        HashSet<Long> basket = new HashSet<Long>();
        basket.add(1L);
        basket.add(7L);
        LongList recs = gRecommender.globalRecommend(basket, -1);
        assertThat(recs, hasSize(2));
        recs = gRecommender.globalRecommend(basket, 1);
        assertThat(recs, hasSize(1));
        assertTrue(recs.contains(5));

    }

    /**
     * Tests {@code globalRecommend(Set, Set)}.
     */
    @Test
    public void testGlobalItemItemRecommender6() {
        HashSet<Long> basket = new HashSet<Long>();
        basket.add(1L);
        HashSet<Long> candidates = new HashSet<Long>();
        candidates.add(5L);
        candidates.add(10L);
        LongList recs = gRecommender.globalRecommend(basket, candidates);
        assertThat(recs, hasSize(2));
        assertTrue(recs.contains(5));
        assertTrue(recs.contains(10));
        candidates.add(7L);
        recs = gRecommender.globalRecommend(basket, candidates);
        assertThat(recs, hasSize(3));

    }

    /**
     * Tests {@code globalRecommend(Set, int, Set, Set)}.
     */
    @Test
    public void testGlobalItemItemRecommender7() {
        HashSet<Long> basket = new HashSet<Long>();
        basket.add(5L);
        basket.add(10L);
        HashSet<Long> candidates = new HashSet<Long>();
        candidates.add(1L);
        candidates.add(7L);
        HashSet<Long> excludes = new HashSet<Long>();
        LongList recs = gRecommender.globalRecommend(basket, 1, candidates, excludes);
        assertThat(recs, hasSize(1));
        excludes.add(5L);
        recs = gRecommender.globalRecommend(basket, 2, candidates, excludes);
        assertThat(recs, hasSize(2));
        assertTrue(recs.contains(1));
        assertTrue(recs.contains(7));
        excludes.add(1L);
        recs = gRecommender.globalRecommend(basket, 2, candidates, excludes);
        assertThat(recs, hasSize(1));
        assertTrue(recs.contains(7));

    }
}
