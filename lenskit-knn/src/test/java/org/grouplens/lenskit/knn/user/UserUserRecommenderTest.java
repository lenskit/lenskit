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
package org.grouplens.lenskit.knn.user;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
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
import org.grouplens.lenskit.vectors.similarity.PearsonCorrelation;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UserUserRecommenderTest {
    private LenskitRecommender rec;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
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
        EventDAO dao = new EventCollectionDAO(rs);
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        config.bind(ItemScorer.class).to(UserUserItemScorer.class);
        config.bind(NeighborhoodFinder.class).to(SimpleNeighborhoodFinder.class);
        config.within(UserSimilarity.class)
              .bind(VectorSimilarity.class)
              .to(PearsonCorrelation.class);
        // this is the default
/*        factory.setComponent(UserVectorNormalizer.class,
                             VectorNormalizer.class,
                             IdentityVectorNormalizer.class);*/
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config);
        rec = engine.createRecommender();
    }

    /**
     * Tests {@code recommend(long, SparseVector)}.
     */
    @Test
    public void testUserUserRecommender1() {
        ItemRecommender recommender = rec.getItemRecommender();
        assert recommender != null;
        List<ScoredId> recs = recommender.recommend(1);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(2);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(9L));

        recs = recommender.recommend(3);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(6L));

        recs = recommender.recommend(4);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(9L));

        recs = recommender.recommend(5);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(7L));

        recs = recommender.recommend(6);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(6L, 7L));
    }

    /**
     * Tests {@code recommend(long, SparseVector, int)}.
     */
    @Test
    public void testUserUserRecommender2() {
        ItemRecommender recommender = rec.getItemRecommender();
        assert recommender != null;
        List<ScoredId> recs = recommender.recommend(1, -1);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(2, 2);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(9L));

        recs = recommender.recommend(2, -1);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(9L));

        recs = recommender.recommend(2, 1);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(9L));

        recs = recommender.recommend(2, 0);
        assertThat(recs, hasSize(0));

        recs = recommender.recommend(3, 1);
        assertEquals(1, recs.size());
        assertEquals(6, recs.get(0).getId());

        recs = recommender.recommend(3, 0);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(4, 1);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(9L));

        recs = recommender.recommend(5, -1);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(7L));

        recs = recommender.recommend(6, 2);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(6L, 7L));

        recs = recommender.recommend(6, 1);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   anyOf(contains(6L), contains(7L)));

        recs = recommender.recommend(6, 0);
        assertThat(recs, hasSize(0));
    }

    /**
     * Tests {@code recommend(long, SparseVector, Set)}.
     */
    @Test
    public void testUserUserRecommender3() {
        ItemRecommender recommender = rec.getItemRecommender();
        assert recommender != null;

        LongOpenHashSet candidates = new LongOpenHashSet();
        candidates.add(6);
        candidates.add(7);
        candidates.add(8);
        candidates.add(9);

        List<ScoredId> recs = recommender.recommend(1, candidates);
        assertTrue(recs.isEmpty());

        candidates.clear();
        candidates.add(9);
        recs = recommender.recommend(2, candidates);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(9L));

        candidates.clear();
        candidates.add(6);
        candidates.add(7);
        candidates.add(8);
        recs = recommender.recommend(2, candidates);
        assertTrue(recs.isEmpty());

        candidates.add(9);
        recs = recommender.recommend(3, candidates);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(6L));

        recs = recommender.recommend(4, candidates);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(9L));

        recs = recommender.recommend(5, candidates);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(7L));

        candidates.remove(7);
        recs = recommender.recommend(5, candidates);
        assertTrue(recs.isEmpty());

        candidates.add(7);
        recs = recommender.recommend(6, candidates);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(6L, 7L));

        candidates.remove(9);
        recs = recommender.recommend(6, candidates);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(6L, 7L));

        candidates.remove(8);
        recs = recommender.recommend(6, candidates);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(6L, 7L));

        candidates.remove(7);
        recs = recommender.recommend(6, candidates);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(6L));

        candidates.remove(6);
        recs = recommender.recommend(6, candidates);
        assertTrue(recs.isEmpty());
    }

    /**
     * Tests {@code recommend(long, SparseVector, int, Set, Set)}.
     */
    @Test
    public void testUserUserRecommender4() {
        ItemRecommender recommender = rec.getItemRecommender();
        assert recommender != null;

        LongOpenHashSet candidates = new LongOpenHashSet();
        candidates.add(9);
        List<ScoredId> recs = recommender.recommend(2, -1, candidates, null);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(9L));

        recs = recommender.recommend(2, 1, candidates, null);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(9L));

        recs = recommender.recommend(2, 0, candidates, null);
        assertTrue(recs.isEmpty());

        LongOpenHashSet exclude = new LongOpenHashSet();
        exclude.add(9);
        recs = recommender.recommend(2, -1, candidates, exclude);
        assertTrue(recs.isEmpty());

        // FIXME Add tests for default exclude set
        recs = recommender.recommend(5, -1, null, LongSets.EMPTY_SET);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(9L, 7L, 6L, 8L));

        recs = recommender.recommend(5, 5, null, LongSets.EMPTY_SET);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(9L, 7L, 6L, 8L));

        recs = recommender.recommend(5, 4, null, LongSets.EMPTY_SET);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(9L, 7L, 6L, 8L));

        recs = recommender.recommend(5, 3, null, LongSets.EMPTY_SET);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(9L, 7L, 6L));

        recs = recommender.recommend(5, 2, null, LongSets.EMPTY_SET);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(9L, 7L));

        recs = recommender.recommend(5, 1, null, LongSets.EMPTY_SET);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(9L));

        recs = recommender.recommend(5, 0, null, LongSets.EMPTY_SET);
        assertTrue(recs.isEmpty());

        candidates.clear();
        candidates.add(6);
        candidates.add(7);
        recs = recommender.recommend(6, -1, candidates, LongSets.EMPTY_SET);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   containsInAnyOrder(6L, 7L));

        candidates.remove(6);
        recs = recommender.recommend(6, -1, candidates, LongSets.EMPTY_SET);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(7L));

        candidates.remove(7);
        recs = recommender.recommend(6, -1, candidates, LongSets.EMPTY_SET);
        assertTrue(recs.isEmpty());

        candidates.add(6);
        candidates.add(7);
        exclude.add(6);
        recs = recommender.recommend(6, -1, candidates, exclude);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(7L));

        exclude.add(7);
        recs = recommender.recommend(6, -1, candidates, exclude);
        assertTrue(recs.isEmpty());

        exclude.remove(6);
        recs = recommender.recommend(6, -1, candidates, exclude);
        assertThat(Lists.transform(recs, ScoredIds.idFunction()),
                   contains(6L));
    }
}
