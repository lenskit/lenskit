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
package org.lenskit.bias;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.baseline.GlobalMeanRatingItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.EntityFactory;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.util.collections.LongUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Created by MichaelEkstrand on 10/8/2016.
 */
public class BiasItemScorerTest {
    private static final double RATINGS_DAT_MEAN = 3.75;
    private StaticDataSource source;
    private DataAccessObject dao;
    private LenskitConfiguration config;

    @Before
    public void createRatingSource() {
        EntityFactory efac = new EntityFactory();
        List<Rating> rs = new ArrayList<>();
        rs.add(efac.rating(1, 5, 2));
        rs.add(efac.rating(1, 7, 4));
        rs.add(efac.rating(8, 4, 5));
        rs.add(efac.rating(8, 5, 4));

        source = new StaticDataSource();
        source.addSource(rs);
        dao = source.get();

        config = new LenskitConfiguration();
        config.bind(ItemScorer.class).to(BiasItemScorer.class);
    }

    public ItemScorer makeGlobalMean() {
        return new GlobalMeanRatingItemScorer(RatingSummary.create(dao));
    }

    @Test
    public void testGlobalMeanBias() {
        config.bind(BiasModel.class).to(GlobalBiasModel.class);
        ItemScorer pred = LenskitRecommender.build(config, dao).getItemScorer();
        assertThat(pred, notNullValue());
        Result score = pred.score(10L, 2L);
        assertThat(score.getScore(), closeTo(RATINGS_DAT_MEAN, 0.00001));
    }

    @Test
    public void testUserMeanBaseline() {
        config.bind(BiasModel.class).to(UserBiasModel.class);
        ItemScorer pred = LenskitRecommender.build(config, dao).getItemScorer();
        assertThat(pred, notNullValue());

        // unseen item
        assertThat(pred.score(8, 4).getScore(), closeTo(4.5, 0.001));
        // seen item - should be same avg
        assertThat(pred.score(8, 10).getScore(), closeTo(4.5, 0.001));
        // unseen user - should be global mean
        assertThat(pred.score(10, 10).getScore(), closeTo(RATINGS_DAT_MEAN, 0.001));
    }

    @Test
    public void testItemMeanBaseline() {
        config.bind(BiasModel.class).to(ItemBiasModel.class);
        ItemScorer pred = LenskitRecommender.build(config, dao).getItemScorer();
        assertThat(pred, notNullValue());

        // unseen item, should be global mean
        assertThat(pred.score(10, 2).getScore(),
                   closeTo(RATINGS_DAT_MEAN, 0.001));
        // seen item - should be item average
        assertThat(pred.score(10, 5).getScore(),
                   closeTo(3.0, 0.001));
    }

    @Test
    public void testUserItemMeanBaseline() {
        config.bind(BiasModel.class).to(UserItemBiasModel.class);
        ItemScorer pred = LenskitRecommender.build(config, dao).getItemScorer();
        assertThat(pred, notNullValue());

        // we use user 8 - their average offset is 0.5
        // unseen item, should be global mean + user offset
        assertThat(pred.score(8, 10).getScore(),
                   closeTo(RATINGS_DAT_MEAN + 0.5, 0.001));

        // seen item - should be item average + user offset
        assertThat(pred.score(8, 5).getScore(),
                   closeTo(3.5, 0.001));

        // seen item, unknown user - should be item average
        assertThat(pred.score(28, 5).getScore(),
                   closeTo(3, 0.001));
    }

    @Test
    public void testLiveItemMeanBaseline() {
        config.bind(BiasModel.class).to(LiveUserItemBiasModel.class);
        ItemScorer pred = LenskitRecommender.build(config, dao).getItemScorer();
        assertThat(pred, notNullValue());

        // we use user 8 - their average offset is 0.5
        // unseen item, should be global mean + user offset
        assertThat(pred.score(8, 10).getScore(),
                   closeTo(RATINGS_DAT_MEAN + 0.5, 0.001));

        // seen item - should be item average + user offset
        assertThat(pred.score(8, 5).getScore(),
                   closeTo(3.5, 0.001));

        // seen item, unknown user - should be item average
        assertThat(pred.score(28, 5).getScore(),
                   closeTo(3, 0.001));
    }

    @Test
    public void testUserItemMeanBaselineMultiRec() {
        config.bind(BiasModel.class).to(UserItemBiasModel.class);
        ItemScorer pred = LenskitRecommender.build(config, dao).getItemScorer();
        assertThat(pred, notNullValue());

        // we use user 8 - their average offset is 0.5
        // unseen item, should be global mean + user offset
        assertThat(pred.score(8, 10).getScore(),
                   closeTo(RATINGS_DAT_MEAN + 0.5, 0.001));

        // seen item - should be item average + user offset
        assertThat(pred.score(8, 5).getScore(),
                   closeTo(3.5, 0.001));

        ResultMap results = pred.scoreWithDetails(8, LongUtils.packedSet(5, 10));
        assertThat(results.getScore(10),
                   closeTo(RATINGS_DAT_MEAN + 0.5, 0.001));
        assertThat(results.getScore(5),
                   closeTo(3.5, 0.001));

        Map<Long, Double> basic = pred.score(8, LongUtils.packedSet(5, 10));
        assertThat(basic.get(10L),
                   closeTo(RATINGS_DAT_MEAN + 0.5, 0.001));
        assertThat(basic.get(5L),
                   closeTo(3.5, 0.001));
    }
}