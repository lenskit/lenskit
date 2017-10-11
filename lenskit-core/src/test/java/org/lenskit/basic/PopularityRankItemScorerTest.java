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
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.ResultMap;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.InteractionStatistics;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.collections.LongUtils;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PopularityRankItemScorerTest {
    private DataAccessObject dao;
    InteractionStatistics statistics;
    ItemScorer recommender;

    @Before
    public void setUp() {
        List<Rating> ratings = ImmutableList.of(Rating.create(42, 1, 3.2),
                                                Rating.create(39, 1, 2.4),
                                                Rating.create(42, 2, 2.5));
        StaticDataSource source = StaticDataSource.fromList(ratings);
        dao = source.get();
        statistics = InteractionStatistics.create(dao);
        recommender = new PopularityRankItemScorer(statistics);
    }

    @Test
    public void testScoreItems() {
        ResultMap results = recommender.scoreWithDetails(42, LongUtils.packedSet(1, 2, 3));
        assertThat(results.size(), equalTo(3));
        assertThat(results.getScore(1), equalTo(1.0));
        assertThat(results.getScore(2), equalTo(0.5));
        assertThat(results.getScore(3), equalTo(0.0));
    }
}
