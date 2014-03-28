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
package org.grouplens.lenskit.eval.metrics.topn;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.history.History;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ItemSelectorsTest {
    @Test
    public void testAllItem() {
        LongSet items = ItemSelectors.allItems().select(History.forUser(42),
                                                        History.forUser(42),
                                                        LongUtils.packedSet(42, 39, 67));
        assertThat(items, containsInAnyOrder(42L, 39L, 67L));
    }

    @Test
    public void testTestItems() {
        LongSet items = ItemSelectors.testItems().select(
                History.forUser(42),
                History.<Event>forUser(42, Lists.newArrayList(Ratings.make(42, 88, 3.5))),
                LongUtils.packedSet(42, 39, 67));
        assertThat(items, containsInAnyOrder(88L));
    }

    @Test
    public void testTrainingItems() {
        LongSet items = ItemSelectors.trainingItems().select(
                History.<Event>forUser(42, Lists.newArrayList(Ratings.make(42, 88, 3.5))),
                History.forUser(42),
                LongUtils.packedSet(42, 39, 67));
        assertThat(items, containsInAnyOrder(88L));
    }

    @Test
    public void testTestRatingMatchItems() {
        LongSet items = ItemSelectors.testRatingMatches(greaterThanOrEqualTo(3.5))
                                     .select(
                                             History.forUser(42),
                                             History.<Event>forUser(42, Lists.newArrayList(Ratings.make(42, 88, 3.5),
                                                                                           Ratings.make(42, 5, 2.4),
                                                                                           Ratings.make(42, 6, 4.0))),
                                             LongUtils.packedSet(42, 39, 67));
        assertThat(items, containsInAnyOrder(88L, 6L));
    }

    @Test
    public void testTestAddRandom() {
        LongSet items = ItemSelectors.addNRandom(ItemSelectors.testItems(), 1)
                                     .select(
                                             History.forUser(42),
                                             History.<Event>forUser(42, Lists.newArrayList(Ratings.make(42, 88, 3.5))),
                                             LongUtils.packedSet(88, 42, 39, 67));
        assertThat(items, hasSize(2));
        assertThat(items, hasItem(88L));
        assertThat(items, anyOf(hasItem(42L),
                                hasItem(39L),
                                hasItem(67L)));
    }

    @Test
    public void testTestAllBut() {
        LongSet items = ItemSelectors.allItemsExcept(ItemSelectors.testItems())
                                     .select(
                                             History.forUser(42),
                                             History.<Event>forUser(42, Lists.newArrayList(Ratings.make(42, 88, 3.5))),
                                             LongUtils.packedSet(88, 42, 39, 67));
        assertThat(items, containsInAnyOrder(42L, 39L, 67L));;
    }

    @Test
    public void testUnion() {
        LongSet items = ItemSelectors.union(ItemSelectors.testItems(), ItemSelectors.trainingItems()).select(
                History.<Event>forUser(42, Lists.newArrayList(Ratings.make(42, 1, 1), Ratings.make(42, 2, 2))),
                History.<Event>forUser(42, Lists.newArrayList(Ratings.make(42, 2, 2), Ratings.make(42, 3, 3))),
                LongUtils.packedSet(1, 2, 3, 4));
        assertThat(items, containsInAnyOrder(1L, 2L, 3L));
        assertThat(items, hasSize(3));
    }

    @Test
    public void testSetDifference() {
        LongSet items = ItemSelectors.setDifference(ItemSelectors.testItems(), ItemSelectors.trainingItems()).select(
                History.<Event>forUser(42, Lists.newArrayList(Ratings.make(42, 1, 1), Ratings.make(42, 2, 2))),
                History.<Event>forUser(42, Lists.newArrayList(Ratings.make(42, 2, 2), Ratings.make(42, 3, 3))),
                LongUtils.packedSet(1, 2, 3, 4));
        assertThat(items, containsInAnyOrder(3L));
        assertThat(items, hasSize(1));
    }    
    @Test
    public void testNRandomFrom() {
        LongSet items = ItemSelectors.nRandomFrom(ItemSelectors.trainingItems(), 2).select(
                History.<Event>forUser(42, Lists.newArrayList(Ratings.make(42, 88, 3.5),
                                                              Ratings.make(42, 5, 2.4),
                                                              Ratings.make(42, 6, 4.0))),
                History.forUser(42),
                LongUtils.packedSet(88,5,6,7));
        
        assertThat(items, hasSize(2));
        assertThat(items, not(hasItem(7L)));
        assertThat(LongUtils.setDifference(LongUtils.packedSet(88, 5, 6), items), hasSize(1));
    }
}
