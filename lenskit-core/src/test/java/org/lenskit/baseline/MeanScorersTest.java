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
package org.lenskit.baseline;


import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.PrefetchingUserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.data.summary.RatingSummary;
import org.lenskit.util.collections.LongUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test baseline predictors that compute means from data.
 */
public class MeanScorersTest {
    private static final double RATINGS_DAT_MEAN = 3.75;
    private EventDAO dao;

    @Before
    public void createRatingSource() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));

        dao = EventCollectionDAO.create(rs);
    }

    LongSortedSet itemSet(long item) {
        return LongUtils.packedSet(item);
    }

    public ItemScorer makeGlobalMean() {
        return new GlobalMeanRatingItemScorer(RatingSummary.create(dao));
    }

    @Test
    public void testMeanBaseline() {
        ItemScorer pred = makeGlobalMean();
        Result score = pred.score(10L, 2L);
        assertThat(score.getScore(), closeTo(RATINGS_DAT_MEAN, 0.00001));
    }

    @Test
    public void testUserMeanBaseline() {
        ItemScorer mean = makeGlobalMean();
        ItemScorer pred = new UserMeanItemScorer(new PrefetchingUserEventDAO(dao),
                                                 mean, new RatingVectorUserHistorySummarizer(),
                                                 0);
        // unseen item
        assertThat(pred.score(8, 4).getScore(), closeTo(4.5, 0.001));
        // seen item - should be same avg
        assertThat(pred.score(8, 10).getScore(), closeTo(4.5, 0.001));
        // unseen user - should be global mean
        assertThat(pred.score(10, 10).getScore(), closeTo(RATINGS_DAT_MEAN, 0.001));
    }

    /**
     * Test falling back to an empty user.
     */
    @Test
    public void testUserMeanBaselineFallback() {
        ItemScorer mean = makeGlobalMean();
        ItemScorer pred = new UserMeanItemScorer(new PrefetchingUserEventDAO(dao),
                                                 mean, new RatingVectorUserHistorySummarizer(),
                                                 0);
        // unseen user - should be global mean
        assertThat(pred.score(10, 10).getScore(), closeTo(RATINGS_DAT_MEAN, 0.001));
    }

    @Test
    public void testItemMeanBaseline() {
        ItemScorer pred = new ItemMeanRatingItemScorer(RatingSummary.create(dao), 0.0);
        // unseen item, should be global mean
        assertThat(pred.score(10, 2).getScore(),
                   closeTo(RATINGS_DAT_MEAN, 0.001));
        // seen item - should be item average
        assertThat(pred.score(10, 5).getScore(),
                   closeTo(3.0, 0.001));
    }

    @Test
    public void testUserItemMeanBaseline() {
        ItemScorer base = new ItemMeanRatingItemScorer(RatingSummary.create(dao), 0.0);
        ItemScorer pred = new UserMeanItemScorer(new PrefetchingUserEventDAO(dao),
                                                 base, new RatingVectorUserHistorySummarizer(),
                                                 0);
        // we use user 8 - their average offset is 0.5
        // unseen item, should be global mean + user offset
        assertThat(pred.score(8, 10).getScore(),
                   closeTo(RATINGS_DAT_MEAN + 0.5, 0.001));

        // seen item - should be item average + user offset
        assertThat(pred.score(8, 5).getScore(),
                   closeTo(3.5, 0.001));
    }
}
