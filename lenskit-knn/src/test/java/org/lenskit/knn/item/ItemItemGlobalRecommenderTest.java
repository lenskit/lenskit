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
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongSets;
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
import org.lenskit.transform.normalize.DefaultUserVectorNormalizer;
import org.lenskit.transform.normalize.IdentityVectorNormalizer;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.lenskit.transform.normalize.VectorNormalizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ItemItemGlobalRecommenderTest {
    private LenskitRecommender session;
    private ItemBasedItemRecommender gRecommender;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 1, 1));
        rs.add(Rating.create(1, 5, 1));
        rs.add(Rating.create(2, 1, 1));
        rs.add(Rating.create(2, 7, 1));
        rs.add(Rating.create(3, 7, 1));
        rs.add(Rating.create(4, 1, 1));
        rs.add(Rating.create(4, 5, 1));
        rs.add(Rating.create(4, 7, 1));
        rs.add(Rating.create(4, 10, 1));
        StaticDataSource source = StaticDataSource.fromList(rs);
        DataAccessObject dao = source.get();

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemBasedItemScorer.class).to(ItemItemItemBasedItemScorer.class);
        // this is the default
        config.bind(UserVectorNormalizer.class)
              .to(DefaultUserVectorNormalizer.class);
        config.bind(VectorNormalizer.class)
              .to(IdentityVectorNormalizer.class);
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, dao);
        session = engine.createRecommender(dao);
        gRecommender = session.getItemBasedItemRecommender();
    }

    @After
    public void teardown() {
        session.close();
    }

    /**
     * Check that we score items but do not provide scores for items
     * the user has previously rated.
     */
    @Test
    public void testGlobalItemScorerNoRating() {
        long[] queryItems = {1, 10};
        long[] items = {5, 10};
        ItemItemItemBasedItemScorer scorer = session.get(ItemItemItemBasedItemScorer.class);
        assertThat(scorer, notNullValue());
        ResultMap scores = scorer.scoreRelatedItemsWithDetails(LongArrayList.wrap(queryItems), LongArrayList.wrap(items));
        assertThat(scores, notNullValue());
        assertThat(scores.size(), equalTo(2));
        Result r5 = scores.get(5);
        assertThat(r5, notNullValue());
        assertThat(r5.getScore(), not(notANumber()));
        // assertThat(scores.get(10), equalTo(0.0));
    }

    /**
     * Tests {@code globalRecommend(long)}.
     */
    // FIXME Give the test methods for global item-item meaningful names
    @Test
    public void testGlobalItemItemRecommender1() {
        List<Long> recs = gRecommender.recommendRelatedItems(LongSets.singleton(1));
        assertThat(recs.size(), notNullValue());
        recs = gRecommender.recommendRelatedItems(LongSets.singleton(2));
        assertThat(recs, empty());
        recs = gRecommender.recommendRelatedItems(LongSets.singleton(5));
        assertThat(recs.size(), notNullValue());
        recs = gRecommender.recommendRelatedItems(LongSets.singleton(1));
        assertThat(recs.size(), notNullValue());
        recs = gRecommender.recommendRelatedItems(LongSets.singleton(10));
        assertThat(recs.size(), notNullValue());

    }

    /**
     * Tests {@code recommendRelatedItems()(long, int)}.
     */
    @Test
    public void testGlobalItemItemRecommender2() {
        List<Long> recs = gRecommender.recommendRelatedItems(LongSets.singleton(1), 2);
        assertThat(recs, hasSize(2));
        recs = gRecommender.recommendRelatedItems(LongSets.singleton(2), 1);
        assertThat(recs, empty());
        recs = gRecommender.recommendRelatedItems(LongSets.singleton(5), 3);
        assertThat(recs, hasSize(3));
    }

    /**
     * Tests {@code recommendRelatedItems()(long, int, Set, Set)}.
     */
    @Test
    public void testGlobalItemItemRecommender4() {
        HashSet<Long> candidates = new HashSet<>();
        HashSet<Long> excludes = new HashSet<>();
        List<Long> recs = gRecommender.recommendRelatedItems(LongSets.singleton(1), 1, candidates, excludes);
        assertThat(recs, hasSize(0));
        candidates.add(7L);
        candidates.add(5L);
        excludes.add(5L);
        recs = gRecommender.recommendRelatedItems(LongSets.singleton(1), 2, candidates, excludes);
        assertThat(recs, hasSize(1));
        assertThat(recs, contains(7L));
        recs = gRecommender.recommendRelatedItems(LongSets.singleton(1), -1, candidates, excludes);
        assertThat(recs, hasSize(1));
        assertThat(recs, contains(7L));

    }

    /**
     * Tests {@code recommendRelatedItems()(Set, int)}.
     */
    @Test
    public void testGlobalItemItemRecommender5() {
        HashSet<Long> basket = new HashSet<>();
        basket.add(1L);
        basket.add(7L);
        List<Long> recs = gRecommender.recommendRelatedItems(basket, -1);
        assertThat(recs, hasSize(2));
        recs = gRecommender.recommendRelatedItems(basket, 1);
        assertThat(recs, contains(5L));
    }

    /**
     * Tests {@code recommendRelatedItems()(Set, int, Set, Set)}.
     */
    @Test
    public void testGlobalItemItemRecommender7() {
        HashSet<Long> basket = new HashSet<>();
        basket.add(5L);
        basket.add(10L);
        HashSet<Long> candidates = new HashSet<>();
        candidates.add(1L);
        candidates.add(7L);
        HashSet<Long> excludes = new HashSet<>();
        List<Long> recs = gRecommender.recommendRelatedItems(basket, 1, candidates, excludes);
        assertThat(recs, hasSize(1));
        excludes.add(5L);
        recs = gRecommender.recommendRelatedItems(basket, 2, candidates, excludes);
        assertThat(recs, contains(1L, 7L));
        excludes.add(1L);
        recs = gRecommender.recommendRelatedItems(basket, 2, candidates, excludes);
        assertThat(recs, contains(7L));

    }
}
