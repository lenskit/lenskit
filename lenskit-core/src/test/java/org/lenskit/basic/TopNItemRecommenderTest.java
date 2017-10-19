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
package org.lenskit.basic;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.junit.Test;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.ResultList;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entities;
import org.lenskit.results.Results;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TopNItemRecommenderTest {
    @Test
    public void testNoScores() {
        StaticDataSource source = new StaticDataSource();
        DataAccessObject dao = source.get();
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                                 .build();
        ItemRecommender rec = new TopNItemRecommender(dao, scorer);

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
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                                 .addScore(42, 3, 3.5)
                                                 .build();
        ItemRecommender rec = new TopNItemRecommender(dao, scorer);

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
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                                 .addScore(42, 3, 3.5)
                                                 .build();
        ItemRecommender rec = new TopNItemRecommender(dao, scorer);

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
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                                                 .addScore(42, 2, 3.0)
                                                 .addScore(42, 7, 1.0)
                                                 .addScore(42, 3, 3.5)
                                                 .build();
        ItemRecommender rec = new TopNItemRecommender(dao, scorer);

        List<Long> recs = rec.recommend(42, 2, null, null);
        assertThat(recs, hasSize(2));
        assertThat(recs, contains(3L, 2L));

        ResultList details = rec.recommendWithDetails(42, 2, null, null);
        assertThat(details, hasSize(2));
        assertThat(details.idList(), contains(3L, 2L));
    }
}
