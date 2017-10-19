/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.*;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;
import org.lenskit.similarity.VectorSimilarity;
import org.lenskit.transform.normalize.DefaultUserVectorNormalizer;
import org.lenskit.transform.normalize.IdentityVectorNormalizer;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.lenskit.transform.normalize.VectorNormalizer;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.*;

public class ItemItemRecommenderTest {

    private DataAccessObject data;
    private LenskitConfiguration config;
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
        data = StaticDataSource.fromList(rs).get();
        config = new LenskitConfiguration();
        config.bind(ItemScorer.class).to(ItemItemScorer.class);
        // this is the default
        config.bind(UserVectorNormalizer.class)
              .to(DefaultUserVectorNormalizer.class);
        config.bind(VectorNormalizer.class)
              .to(IdentityVectorNormalizer.class);
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, data);
        session = engine.createRecommender(data);
        recommender = session.getItemRecommender();
    }

    @After
    public void teardown() {
        session.close();
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

    @Test
    public void testItemItemRecommenderNonSymmetric() {
        config.bind(ItemSimilarity.class)
              .to(NonSymmetricSimilarity.class);
        session = LenskitRecommender.build(config, data);
        recommender = session.getItemRecommender();

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

    @Test
    public void testRecommendWithMinCommonUsers() {
        config.set(MinCommonUsers.class).to(1);
        session = LenskitRecommenderEngine.build(config, data).createRecommender(data);
        recommender = session.getItemRecommender();
        List<Long> recs = recommender.recommend(1);
        assertThat(recs, hasSize(0));

        recs = recommender.recommend(2);
        assertThat(recs, contains(9L));
    }

    @Test
    public void testRecommendWithMinCommonUsers3() {
        config.set(MinCommonUsers.class).to(3);
        session = LenskitRecommenderEngine.build(config, data).createRecommender(data);
        recommender = session.getItemRecommender();
        List<Long> recs = recommender.recommend(2);
        assertThat(recs, hasSize(0));
    }

    public static class NonSymmetricSimilarity implements ItemSimilarity {
        final VectorSimilarity delegate;

        @Inject
        public NonSymmetricSimilarity(VectorSimilarity dlg) {
            delegate = dlg;
        }

        @Override
        public double similarity(long i1, Long2DoubleMap v1, long i2, Long2DoubleMap v2) {
            return delegate.similarity(v1, v2);
        }

        @Override
        public boolean isSparse() {
            return delegate.isSparse();
        }

        @Override
        public boolean isSymmetric() {
            return false;
        }
    }
}
