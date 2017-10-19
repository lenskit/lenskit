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
package org.lenskit.knn.user;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.*;
import org.lenskit.basic.SimpleRatingPredictor;
import org.lenskit.basic.TopNItemRecommender;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UserUserRecommenderBuildTest {

    private static RecommenderEngine engine;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));

        StaticDataSource source = StaticDataSource.fromList(rs);
        DataAccessObject dao = source.get();

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(DataAccessObject.class).to(dao);
        config.bind(ItemScorer.class).to(UserUserItemScorer.class);
        config.bind(NeighborFinder.class).to(LiveNeighborFinder.class);

        engine = LenskitRecommenderEngine.build(config);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUserUserRecommenderEngineCreate() {
        try (Recommender rec = engine.createRecommender()) {

            assertThat(rec.getItemScorer(),
                       instanceOf(UserUserItemScorer.class));
            assertThat(rec.getItemRecommender(),
                       instanceOf(TopNItemRecommender.class));
            RatingPredictor pred = rec.getRatingPredictor();
            assertThat(pred, notNullValue());
            assertThat(pred, instanceOf(SimpleRatingPredictor.class));
            assertThat(((SimpleRatingPredictor) pred).getItemScorer(),
                       sameInstance(rec.getItemScorer()));
        }
    }

    @Test
    public void testSnapshot() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));

        StaticDataSource source = StaticDataSource.fromList(rs);
        DataAccessObject dao = source.get();

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemScorer.class).to(UserUserItemScorer.class);
        config.bind(NeighborFinder.class).to(SnapshotNeighborFinder.class);

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, dao);
        try (Recommender rec = engine.createRecommender(dao)) {
            assertThat(rec.getItemScorer(),
                       instanceOf(UserUserItemScorer.class));
            assertThat(rec.getItemRecommender(),
                       instanceOf(TopNItemRecommender.class));
            RatingPredictor pred = rec.getRatingPredictor();
            assertThat(pred, instanceOf(SimpleRatingPredictor.class));
            try (Recommender rec2 = engine.createRecommender(dao)) {
                assertThat(rec2.getItemScorer(), not(sameInstance(rec.getItemScorer())));
            }
        }
    }
}
