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
package org.lenskit.knn.user;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.lenskit.similarity.PearsonCorrelation;
import org.lenskit.similarity.VectorSimilarity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UserUserRecommenderTest {
    private LenskitRecommender rec;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 6, 4));
        rs.add(Rating.create(2, 6, 2));
        rs.add(Rating.create(4, 6, 3));
        rs.add(Rating.create(5, 6, 4));
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
        rs.add(Rating.create(6, 9, 4));
        rs.add(Rating.create(5, 9, 4));
        StaticDataSource source = StaticDataSource.fromList(rs);
        DataAccessObject dao = source.get();
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemScorer.class).to(UserUserItemScorer.class);
        config.bind(NeighborFinder.class).to(LiveNeighborFinder.class);
        config.within(UserSimilarity.class)
              .bind(VectorSimilarity.class)
              .to(PearsonCorrelation.class);
        // this is the default
/*        factory.setComponent(UserVectorNormalizer.class,
                             VectorNormalizer.class,
                             IdentityVectorNormalizer.class);*/
        rec = LenskitRecommender.build(config, dao);
    }

    @After
    public void teardown() {
        rec.close();
    }

    /**
     * Tests {@code recommend(long, SparseVector)}.
     */
    @Test
    public void testUserUserRecommender1() {
        ItemRecommender recommender = rec.getItemRecommender();
        assert recommender != null;
        List<Long> recs = recommender.recommend(1);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(2);
        assertThat(recs,
                   containsInAnyOrder(9L));

        recs = recommender.recommend(3);
        assertThat(recs,
                   containsInAnyOrder(6L));

        recs = recommender.recommend(4);
        assertThat(recs,
                   containsInAnyOrder(9L));

        recs = recommender.recommend(5);
        assertThat(recs,
                   containsInAnyOrder(7L));

        recs = recommender.recommend(6);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L));
    }

    /**
     * Tests {@code recommend(long, SparseVector, int)}.
     */
    @Test
    public void testUserUserRecommender2() {
        ItemRecommender recommender = rec.getItemRecommender();
        assert recommender != null;
        List<Long> recs = recommender.recommend(1, -1);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(2, 2);
        assertThat(recs,
                   containsInAnyOrder(9L));

        recs = recommender.recommend(2, -1);
        assertThat(recs,
                   containsInAnyOrder(9L));

        recs = recommender.recommend(2, 1);
        assertThat(recs,
                   containsInAnyOrder(9L));

        recs = recommender.recommend(2, 0);
        assertThat(recs, hasSize(0));

        recs = recommender.recommend(3, 1);
        assertEquals(1, recs.size());
        assertThat(recs.get(0), equalTo(6L));

        recs = recommender.recommend(3, 0);
        assertTrue(recs.isEmpty());

        recs = recommender.recommend(4, 1);
        assertThat(recs,
                   containsInAnyOrder(9L));

        recs = recommender.recommend(5, -1);
        assertThat(recs,
                   containsInAnyOrder(7L));

        recs = recommender.recommend(6, 2);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L));

        recs = recommender.recommend(6, 1);
        assertThat(recs,
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

        List<Long> recs = recommender.recommend(1, -1, candidates, null);
        assertTrue(recs.isEmpty());

        candidates.clear();
        candidates.add(9);
        recs = recommender.recommend(2, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(9L));

        candidates.clear();
        candidates.add(6);
        candidates.add(7);
        candidates.add(8);
        recs = recommender.recommend(2, -1, candidates, null);
        assertTrue(recs.isEmpty());

        candidates.add(9);
        recs = recommender.recommend(3, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(6L));

        recs = recommender.recommend(4, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(9L));

        recs = recommender.recommend(5, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(7L));

        candidates.remove(7);
        recs = recommender.recommend(5, -1, candidates, null);
        assertTrue(recs.isEmpty());

        candidates.add(7);
        recs = recommender.recommend(6, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L));

        candidates.remove(9);
        recs = recommender.recommend(6, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L));

        candidates.remove(8);
        recs = recommender.recommend(6, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L));

        candidates.remove(7);
        recs = recommender.recommend(6, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(6L));

        candidates.remove(6);
        recs = recommender.recommend(6, -1, candidates, null);
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
        List<Long> recs = recommender.recommend(2, -1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(9L));

        recs = recommender.recommend(2, 1, candidates, null);
        assertThat(recs,
                   containsInAnyOrder(9L));

        recs = recommender.recommend(2, 0, candidates, null);
        assertTrue(recs.isEmpty());

        LongOpenHashSet exclude = new LongOpenHashSet();
        exclude.add(9);
        recs = recommender.recommend(2, -1, candidates, exclude);
        assertTrue(recs.isEmpty());

        // FIXME Add tests for default exclude set
        recs = recommender.recommend(5, -1, null, LongSets.EMPTY_SET);
        assertThat(recs,
                   contains(9L, 7L, 6L, 8L));

        recs = recommender.recommend(5, 5, null, LongSets.EMPTY_SET);
        assertThat(recs,
                   contains(9L, 7L, 6L, 8L));

        recs = recommender.recommend(5, 4, null, LongSets.EMPTY_SET);
        assertThat(recs,
                   contains(9L, 7L, 6L, 8L));

        recs = recommender.recommend(5, 3, null, LongSets.EMPTY_SET);
        assertThat(recs,
                   contains(9L, 7L, 6L));

        recs = recommender.recommend(5, 2, null, LongSets.EMPTY_SET);
        assertThat(recs,
                   contains(9L, 7L));

        recs = recommender.recommend(5, 1, null, LongSets.EMPTY_SET);
        assertThat(recs,
                   contains(9L));

        recs = recommender.recommend(5, 0, null, LongSets.EMPTY_SET);
        assertTrue(recs.isEmpty());

        candidates.clear();
        candidates.add(6);
        candidates.add(7);
        recs = recommender.recommend(6, -1, candidates, LongSets.EMPTY_SET);
        assertThat(recs,
                   containsInAnyOrder(6L, 7L));

        candidates.remove(6);
        recs = recommender.recommend(6, -1, candidates, LongSets.EMPTY_SET);
        assertThat(recs,
                   contains(7L));

        candidates.remove(7);
        recs = recommender.recommend(6, -1, candidates, LongSets.EMPTY_SET);
        assertTrue(recs.isEmpty());

        candidates.add(6);
        candidates.add(7);
        exclude.add(6);
        recs = recommender.recommend(6, -1, candidates, exclude);
        assertThat(recs,
                   contains(7L));

        exclude.add(7);
        recs = recommender.recommend(6, -1, candidates, exclude);
        assertTrue(recs.isEmpty());

        exclude.remove(6);
        recs = recommender.recommend(6, -1, candidates, exclude);
        assertThat(recs,
                   contains(6L));
    }
}
