/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.junit.Test;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.ResultList;
import org.lenskit.data.dao.*;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entities;
import org.lenskit.results.Results;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TopNItemRecommenderTest {
    @Test
    public void testNoScores() {
        StaticDataSource source = new StaticDataSource();
        DataAccessObject dao = source.get();
        ItemDAO idao = new BridgeItemDAO(dao);
        UserEventDAO uedao = new BridgeUserEventDAO(dao);
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
        StaticDataSource source = new StaticDataSource();
        source.addSource(ImmutableList.of(Entities.create(CommonTypes.ITEM, 3)));
        DataAccessObject dao = source.get();
        ItemDAO idao = new BridgeItemDAO(dao);
        UserEventDAO uedao = new BridgeUserEventDAO(dao);
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
        StaticDataSource source = new StaticDataSource();
        source.addSource(ImmutableList.of(Entities.create(CommonTypes.ITEM, 3)));
        DataAccessObject dao = source.get();
        ItemDAO idao = new BridgeItemDAO(dao);
        UserEventDAO uedao = new BridgeUserEventDAO(dao);
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
        StaticDataSource source = new StaticDataSource();
        source.addSource(ImmutableList.of(Entities.create(CommonTypes.ITEM, 3),
                                          Entities.create(CommonTypes.ITEM, 2),
                                          Entities.create(CommonTypes.ITEM, 7)));
        DataAccessObject dao = source.get();
        ItemDAO idao = new BridgeItemDAO(dao);
        UserEventDAO uedao = new BridgeUserEventDAO(dao);
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
