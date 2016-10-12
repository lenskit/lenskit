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
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.ResultMap;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.util.collections.LongUtils;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PopularityRankItemScorerTest {
    private DataAccessObject dao;
    RatingSummary summary;
    ItemScorer recommender;

    @Before
    public void setUp() {
        List<Rating> ratings = ImmutableList.of(Rating.create(42, 1, 3.2),
                                                Rating.create(39, 1, 2.4),
                                                Rating.create(42, 2, 2.5));
        StaticDataSource source = StaticDataSource.fromList(ratings);
        dao = source.get();
        summary = RatingSummary.create(dao);
        recommender = new PopularityRankItemScorer(summary);
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
