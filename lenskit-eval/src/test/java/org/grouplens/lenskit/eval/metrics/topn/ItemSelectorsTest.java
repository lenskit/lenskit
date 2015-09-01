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
package org.grouplens.lenskit.eval.metrics.topn;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.lenskit.data.ratings.Rating;
import org.grouplens.lenskit.eval.traintest.MockTestUser;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.util.collections.LongUtils;

import javax.inject.Inject;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ItemSelectorsTest {
    static class FixedItemDAO implements ItemDAO {
        @Inject
        public FixedItemDAO() {}

        @Override
        public LongSet getItemIds() {
            return LongUtils.packedSet(42, 39, 67);
        }
    }

    LenskitRecommender recommender;

    @Before
    public void createRecommender() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.addRoot(ItemDAO.class);
        config.bind(ItemDAO.class).to(FixedItemDAO.class);
        config.bind(EventDAO.class).to(EventCollectionDAO.create(Collections.<Rating>emptyList()));
        recommender = LenskitRecommender.build(config);
    }

    @Test
    public void testAllItem() {
        TestUser user = MockTestUser.newBuilder()
                                    .setUserId(42)
                                    .setRecommender(recommender)
                                    .build();
        LongSet items = ItemSelectors.allItems().select(user);
        assertThat(items, containsInAnyOrder(42L, 39L, 67L));
    }

    @Test
    public void testTestItems() {
        TestUser user = MockTestUser.newBuilder()
                                    .setUserId(42)
                                    .addTestRating(88, 3.5)
                                    .setRecommender(recommender)
                                    .build();
        LongSet items = ItemSelectors.testItems().select(user);
        assertThat(items, containsInAnyOrder(88L));
    }

    @Test
    public void testTrainingItems() {
        TestUser user = MockTestUser.newBuilder()
                                    .setUserId(42)
                                    .addTrainRating(88, 3.5)
                                    .setRecommender(recommender)
                                    .build();
        LongSet items = ItemSelectors.trainingItems().select(user);
        assertThat(items, containsInAnyOrder(88L));
    }

    @Test
    public void testTestRatingMatchItems() {
        TestUser user = MockTestUser.newBuilder()
                                    .setUserId(42)
                                    .addTestRating(88, 3.5)
                                    .addTestRating(5, 2.4)
                                    .addTestRating(6, 4.0)
                                    .setRecommender(recommender)
                                    .build();
        LongSet items = ItemSelectors.testRatingMatches(greaterThanOrEqualTo(3.5))
                                     .select(user);
        assertThat(items, containsInAnyOrder(88L, 6L));
    }

    @Test
    public void testTestAddRandom() {
        TestUser user = MockTestUser.newBuilder()
                                    .setUserId(42)
                                    .addTestRating(88, 3.5)
                                    .setRecommender(recommender)
                                    .build();
        LongSet items = ItemSelectors.addNRandom(ItemSelectors.testItems(), 1)
                                     .select(user);
        assertThat(items, hasSize(2));
        assertThat(items, hasItem(88L));
        assertThat(items, anyOf(hasItem(42L),
                                hasItem(39L),
                                hasItem(67L)));
    }

    @Test
    public void testTestAllBut() {
        TestUser user = MockTestUser.newBuilder()
                                    .setUserId(42)
                                    .addTestRating(67, 3.5)
                                    .setRecommender(recommender)
                                    .build();
        LongSet items = ItemSelectors.allItemsExcept(ItemSelectors.testItems())
                                     .select(user);
        assertThat(items, containsInAnyOrder(42L, 39L));
    }

    @Test
    public void testUnion() {
        TestUser user = MockTestUser.newBuilder()
                                    .setUserId(42)
                                    .addTrainRating(1, 1)
                                    .addTrainRating(2, 2)
                                    .addTestRating(2, 2)
                                    .addTestRating(3, 3)
                                    .setRecommender(recommender)
                                    .build();
        LongSet items = ItemSelectors.union(ItemSelectors.testItems(),
                                            ItemSelectors.trainingItems())
                                     .select(user);
        assertThat(items, containsInAnyOrder(1L, 2L, 3L));
        assertThat(items, hasSize(3));
    }

    @Test
    public void testSetDifference() {
        TestUser user = MockTestUser.newBuilder()
                                    .setUserId(42)
                                    .addTrainRating(1, 1)
                                    .addTrainRating(2, 2)
                                    .addTestRating(2, 2)
                                    .addTestRating(3, 3)
                                    .setRecommender(recommender)
                                    .build();
        LongSet items = ItemSelectors.setDifference(ItemSelectors.testItems(),
                                                    ItemSelectors.trainingItems())
                                     .select(user);
        assertThat(items, containsInAnyOrder(3L));
        assertThat(items, hasSize(1));
    }    
    @Test
    public void testNRandomFrom() {
        TestUser user = MockTestUser.newBuilder()
                                    .setUserId(42)
                                    .addTrainRating(88, 3.5)
                                    .addTrainRating(5, 2.4)
                                    .addTrainRating(6, 4.0)
                                    .setRecommender(recommender)
                                    .build();
        LongSet items = ItemSelectors.randomSubset(ItemSelectors.trainingItems(), 2)
                                     .select(user);

        assertThat(items, hasSize(2));
        assertThat(items, not(hasItem(7L)));
        assertThat(LongUtils.setDifference(LongUtils.packedSet(88, 5, 6), items), hasSize(1));
    }
}
