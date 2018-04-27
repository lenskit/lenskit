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
package org.lenskit.baseline;


import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.data.ratings.StandardRatingVectorPDAO;
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
    private StaticDataSource source;
    private DataAccessObject dao;

    @Before
    public void createRatingSource() {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));

        source = new StaticDataSource();
        source.addSource(rs);
        dao = source.get();
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
        ItemScorer pred = new UserMeanItemScorer(new StandardRatingVectorPDAO(dao),
                                                 mean,
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
        ItemScorer pred = new UserMeanItemScorer(new StandardRatingVectorPDAO(dao),
                                                 mean,
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
        ItemScorer pred = new UserMeanItemScorer(new StandardRatingVectorPDAO(dao),
                                                 base,
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
