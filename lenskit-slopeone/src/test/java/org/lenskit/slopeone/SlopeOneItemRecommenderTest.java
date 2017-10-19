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
package org.lenskit.slopeone;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.baseline.ItemMeanRatingItemScorer;
import org.lenskit.baseline.UserMeanBaseline;
import org.lenskit.baseline.UserMeanItemScorer;
import org.lenskit.basic.SimpleRatingPredictor;
import org.lenskit.basic.TopNItemRecommender;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.Rating;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SlopeOneItemRecommenderTest {
    private DataAccessObject dao;
    private LenskitRecommenderEngine engine;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));

        StaticDataSource source = StaticDataSource.fromList(rs);
        dao = source.get();

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemScorer.class).to(SlopeOneItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomain(1, 5));
        // factory.setComponent(UserVectorNormalizer.class, IdentityVectorNormalizer.class);
        config.bind(BaselineScorer.class, ItemScorer.class)
              .to(UserMeanItemScorer.class);
        config.bind(UserMeanBaseline.class, ItemScorer.class)
              .to(ItemMeanRatingItemScorer.class);
        engine = LenskitRecommenderEngine.build(config, dao);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSlopeOneRecommenderEngineCreate() {
        try (Recommender rec = engine.createRecommender(dao)) {

            assertThat(rec.getItemScorer(),
                       instanceOf(SlopeOneItemScorer.class));
            RatingPredictor rp = rec.getRatingPredictor();
            assertThat(rp, notNullValue());
            assertThat(rp, instanceOf(SimpleRatingPredictor.class));
            assertThat(((SimpleRatingPredictor) rp).getItemScorer(),
                       sameInstance(rec.getItemScorer()));
            assertThat(rec.getItemRecommender(),
                       instanceOf(TopNItemRecommender.class));
        }
    }

    @Test
    public void testConfigSeparation() {
        try (LenskitRecommender rec1 = engine.createRecommender(dao);
             LenskitRecommender rec2 = engine.createRecommender(dao)) {

            assertThat(rec1.getItemScorer(),
                       not(sameInstance(rec2.getItemScorer())));
            assertThat(rec1.get(SlopeOneModel.class),
                       allOf(not(nullValue()),
                             sameInstance(rec2.get(SlopeOneModel.class))));
        }
    }
}
