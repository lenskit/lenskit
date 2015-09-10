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
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.lenskit.data.ratings.Rating;
import org.grouplens.lenskit.transform.normalize.DefaultUserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.IdentityVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.VectorNormalizer;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.*;

public class ItemItemRecommenderTest {

    private LenskitRecommender session;
    private ItemRecommender recommender;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 6, 4));
        rs.add(Rating.create(2, 6, 2));
        rs.add(Rating.create(1, 7, 3));
        rs.add(Rating.create(2, 7, 2));
        rs.add(Rating.create(3, 7, 5));
        rs.add(Rating.create(4, 7, 2));
        rs.add(Rating.create(1, 8, 3));
        rs.add(Rating.create(2, 8, 4));
        rs.add(Rating.create(3, 8, 3));
        rs.add(Rating.create(4, 8, 2));
        rs.add(Rating.create(5, 8, 3));
        rs.add(Rating.create(6, 8, 2));
        rs.add(Rating.create(1, 9, 3));
        rs.add(Rating.create(3, 9, 4));
        EventCollectionDAO dao = new EventCollectionDAO(rs);
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        config.bind(ItemScorer.class).to(ItemItemScorer.class);
        // this is the default
        config.bind(UserVectorNormalizer.class)
              .to(DefaultUserVectorNormalizer.class);
        config.bind(VectorNormalizer.class)
              .to(IdentityVectorNormalizer.class);
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config);
        session = engine.createRecommender();
        recommender = session.getItemRecommender();
    }

    /**
     * Check that we score items but do not provide scores for items
     * the user has previously rated.  User 5 has rated only item 8
     * previously.
     */
    @Test
    public void testItemScorerNoRating() {
        long[] items = {7, 8};
        ItemItemScorer scorer = session.get(ItemItemScorer.class);
        assertThat(scorer, notNullValue());
        Map<Long, Double> scores = scorer.score(5, LongArrayList.wrap(items));
        assertThat(scores, notNullValue());
        assertThat(scores.size(), equalTo(1));
        assertThat(scores.get(7L), not(notANumber()));
        assertThat(scores.containsKey(8L), equalTo(false));
    }

    /**
     * Check that we score items but do not provide scores for items
     * the user has previously rated.  User 5 has rated only item 8
     * previously.
     */
    @Test
    public void testItemScorerChannels() {
        long[] items = {7, 8};
        ItemItemScorer scorer = session.get(ItemItemScorer.class);
        assertThat(scorer, notNullValue());
        Map<Long, Double> scores = scorer.score(5, LongArrayList.wrap(items));
        assertThat(scores, notNullValue());
        assertThat(scores.size(), equalTo(1));
        assertThat(scores.containsKey(8L), equalTo(false));

        long[] items2 = {7, 8, 9};
        scorer = session.get(ItemItemScorer.class);
        assertThat(scorer, notNullValue());
        ResultMap details = scorer.scoreWithDetails(2, LongArrayList.wrap(items2));
        Result r = details.get(9);
        assertThat(r, notNullValue());
        ItemItemResult score = r.as(ItemItemResult.class);
        assertThat(score, notNullValue());
        assertThat(score.getNeighborhoodSize(), equalTo(3));
    }

    /**
     * Tests {@code recommend(long, SparseVector)}.
     */
    @Test
    public void testItemItemRecommender1() {
        List<Long> recs = recommender.recommend(1);
        assertThat(recs, hasSize(0));

        recs = recommender.recommend(2);
        assertThat(recs,
                   contains(9L));

        recs = recommender.recommend(3);
        assertThat(recs,
                   contains(6L));

        recs = recommender.recommend(4);
        assertThat(recs,
                   containsInAnyOrder(6L, 9L));
        assertEquals(2, recs.size());

        recs = recommender.recommend(5);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L, 9L));

        recs = recommender.recommend(6);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L, 9L));
    }

    /**
     * Tests {@code recommend(long, SparseVector, int)}.
     */
    @Test
    public void testItemItemRecommender2() {
        List<Long> recs = recommender.recommend(2, 1);
        assertThat(recs,
                   contains(9L));

        recs = recommender.recommend(2, 0);
        assertThat(recs, hasSize(0));

        recs = recommender.recommend(3, 1);
        assertThat(recs,
                   anyOf(contains(6L),
                         contains(9L)));

        recs = recommender.recommend(4, 0);
        assertThat(recs, hasSize(0));
    }

    /**
     * Tests {@code recommend(long, SparseVector, Set)}.
     */
    @Test
    public void testItemItemRecommender3() {
        List<Long> recs = recommender.recommend(1, -1, null, null);
        assertTrue(recs.isEmpty());


        LongOpenHashSet candidates = new LongOpenHashSet();
        candidates.add(6);
        candidates.add(7);
        candidates.add(8);
        candidates.add(9);
        recs = recommender.recommend(1, -1, candidates, null);
        assertThat(recs, hasSize(0));

        recs = recommender.recommend(2, -1, null, null);
        assertThat(recs,
                   contains(9L));

        candidates.clear();
        candidates.add(7);
        candidates.add(8);
        candidates.add(9);
        recs = recommender.recommend(2, -1, candidates, null);
        assertThat(recs,
                   contains(9L));

        candidates.add(6);
        candidates.remove(9);
        recs = recommender.recommend(2, -1, candidates, null);
        assertThat(recs, hasSize(0));

        recs = recommender.recommend(5, -1, null, null);
        assertThat(recs,
                   containsInAnyOrder(9L, 7L, 6L));

        candidates.clear();
        candidates.add(6);
        candidates.add(7);
        recs = recommender.recommend(5, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L));

        candidates.clear();
        candidates.add(6);
        candidates.add(9);
        recs = recommender.recommend(5, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(6L, 9L));
    }

    /**
     * Tests {@code recommend(long, SparseVector, int, Set, Set)}.
     */
    @Test
    public void testItemItemRecommender4() {
        List<Long> recs = recommender.recommend(5, -1, null, null);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L, 9L));

        LongOpenHashSet candidates = new LongOpenHashSet();
        candidates.add(6);
        candidates.add(7);
        candidates.add(8);
        candidates.add(9);
        recs = recommender.recommend(5, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L, 9L));

        candidates.remove(6);
        recs = recommender.recommend(5, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(7L, 9L));

        candidates.remove(7);
        recs = recommender.recommend(5, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(9L));

        candidates.remove(9);
        recs = recommender.recommend(5, -1, candidates, null);
        assertThat(recs, hasSize(0));

        candidates.add(9);
        candidates.add(7);
        recs = recommender.recommend(5, 1, candidates, null);
        assertThat(recs,
                   anyOf(contains(9L), contains(7L)));

        LongOpenHashSet exclude = new LongOpenHashSet();
        exclude.add(7);
        recs = recommender.recommend(5, 2, candidates, exclude);
        assertThat(recs,
                   containsInAnyOrder(9L));

        recs = recommender.recommend(5, 0, candidates, null);
        assertThat(recs, hasSize(0));

        candidates.clear();
        candidates.add(7);
        candidates.add(9);
        recs = recommender.recommend(5, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(7L, 9L));

        candidates.add(6);
        exclude.clear();
        exclude.add(9);
        recs = recommender.recommend(5, -1, candidates, exclude);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L));

        exclude.add(7);
        recs = recommender.recommend(5, -1, candidates, exclude);
        assertThat(recs,
                   containsInAnyOrder(6L));

        exclude.add(6);
        recs = recommender.recommend(5, -1, candidates, exclude);
        assertThat(recs, hasSize(0));
    }
}
