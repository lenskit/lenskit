/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.grouplens.lenskit.GlobalItemRecommender;
import org.grouplens.lenskit.GlobalItemScorer;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIds;
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

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ItemItemGlobalRecommenderTest {
    private LenskitRecommender session;
    private GlobalItemRecommender gRecommender;

    @SuppressWarnings("deprecation")
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
        EventCollectionDAO dao = new EventCollectionDAO(rs);
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        config.bind(GlobalItemScorer.class).to(ItemItemGlobalScorer.class);
        // this is the default
        config.bind(UserVectorNormalizer.class)
              .to(DefaultUserVectorNormalizer.class);
        config.bind(VectorNormalizer.class)
              .to(IdentityVectorNormalizer.class);
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config);
        session = engine.createRecommender();
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
        List<ScoredId> recs = gRecommender.globalRecommend(LongSets.singleton(1));
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
        List<ScoredId> recs = gRecommender.globalRecommend(LongSets.singleton(1), 2);
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
        List<ScoredId> recs = gRecommender.globalRecommend(LongSets.singleton(1), candidates);
        assertThat(recs, hasSize(0));
        candidates.add(1L);
        candidates.add(5L);
        recs = gRecommender.globalRecommend(LongSets.singleton(1), candidates);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(5L));

    }

    /**
     * Tests {@code globalRecommend(long, int, Set, Set)}.
     */
    @Test
    public void testGlobalItemItemRecommender4() {
        HashSet<Long> candidates = new HashSet<Long>();
        HashSet<Long> excludes = new HashSet<Long>();
        List<ScoredId> recs = gRecommender.globalRecommend(LongSets.singleton(1), 1, candidates, excludes);
        assertThat(recs, hasSize(0));
        candidates.add(7L);
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
        List<ScoredId> recs = gRecommender.globalRecommend(basket, -1);
        assertThat(recs, hasSize(2));
        recs = gRecommender.globalRecommend(basket, 1);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(5L));
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
        List<ScoredId> recs = gRecommender.globalRecommend(basket, candidates);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(5L, 10L));
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
        List<ScoredId> recs = gRecommender.globalRecommend(basket, 1, candidates, excludes);
        assertThat(recs, hasSize(1));
        excludes.add(5L);
        recs = gRecommender.globalRecommend(basket, 2, candidates, excludes);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(1L, 7L));
        excludes.add(1L);
        recs = gRecommender.globalRecommend(basket, 2, candidates, excludes);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(7L));

    }
}
