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