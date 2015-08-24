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
package org.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongLists;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.grouplens.lenskit.data.dao.*;
import org.junit.Test;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.ResultList;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class TopNItemRecommenderTest {
    @Test
    public void testNoScores() {
        EventDAO dao = EventCollectionDAO.empty();
        ItemDAO idao = new PrefetchingItemDAO(dao);
        UserEventDAO uedao = new PrefetchingUserEventDAO(dao);
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                                 .build();
        ItemRecommender rec = new TopNItemRecommender(uedao, idao, scorer);

        List<Long> recs = rec.recommend(42);
        assertThat(recs, hasSize(0));

        ResultList details = rec.recommendWithDetails(42, -1, null, null);
        assertThat(details, hasSize(0));
    }

    @Test
    public void testGetScoreOnly() {
        EventDAO dao = EventCollectionDAO.empty();
        ItemDAO idao = new ItemListItemDAO(LongLists.singleton(3));
        UserEventDAO uedao = new PrefetchingUserEventDAO(dao);
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                                 .addScore(42, 3, 3.5)
                                                 .build();
        ItemRecommender rec = new TopNItemRecommender(uedao, idao, scorer);

        List<Long> recs = rec.recommend(42);
        assertThat(recs, contains(3L));

        ResultList details = rec.recommendWithDetails(42, -1, null, null);
        assertThat(details, hasSize(1));
        assertThat(Results.basicCopy(details.get(0)),
                   equalTo(Results.create(3, 3.5)));
    }

    @Test
    public void testExcludeScore() {
        EventDAO dao = EventCollectionDAO.empty();
        ItemDAO idao = new ItemListItemDAO(LongLists.singleton(3));
        UserEventDAO uedao = new PrefetchingUserEventDAO(dao);
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                                 .addScore(42, 3, 3.5)
                                                 .build();
        ItemRecommender rec = new TopNItemRecommender(uedao, idao, scorer);

        List<Long> recs = rec.recommend(42, -1, null, LongSets.singleton(3L));
        assertThat(recs, hasSize(0));

        ResultList details = rec.recommendWithDetails(42, -1, null,
                                                      LongSets.singleton(3L));
        assertThat(details, hasSize(0));
    }

    @Test
    public void testFindSomeItems() {
        EventDAO dao = EventCollectionDAO.empty();
        ItemDAO idao = new ItemListItemDAO(LongUtils.packedSet(2, 7, 3));
        UserEventDAO uedao = new PrefetchingUserEventDAO(dao);
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                                 .addScore(42, 2, 3.0)
                                                 .addScore(42, 7, 1.0)
                                                 .addScore(42, 3, 3.5)
                                                 .build();
        ItemRecommender rec = new TopNItemRecommender(uedao, idao, scorer);

        List<Long> recs = rec.recommend(42, 2, null, null);
        assertThat(recs, hasSize(2));
        assertThat(recs, contains(3L, 2L));

        ResultList details = rec.recommendWithDetails(42, 2, null, null);
        assertThat(details, hasSize(2));
        assertThat(details.idList(), contains(3L, 2L));
    }
}
