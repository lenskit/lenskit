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

import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.PreferenceDomainBuilder;
import org.lenskit.data.ratings.Rating;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WeightedSlopeOneItemScorerTest {

    private static final double EPSILON = 1.0e-6;

    @Test
    public void testPredict1() throws RecommenderBuildException {

        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 6, 4));
        rs.add(Rating.create(2, 6, 2));
        rs.add(Rating.create(1, 7, 3));
        rs.add(Rating.create(2, 7, 2));
        rs.add(Rating.create(3, 7, 5));
        rs.add(Rating.create(4, 7, 2));
        rs.add(Rating.create(1, 8, 3));
        rs.add(Rating.create(2, 8, 4));
        rs.add(Rating.create(3, 8, 3));
        rs.add(Rating.create(4, 8, 2));
        rs.add(Rating.create(5, 8, 3));
        rs.add(Rating.create(6, 8, 2));
        rs.add(Rating.create(1, 9, 3));
        rs.add(Rating.create(3, 9, 4));

        StaticDataSource source = StaticDataSource.fromList(rs);
        DataAccessObject dao = source.get();

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemScorer.class).to(WeightedSlopeOneItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomainBuilder(1, 5)
                                                       .setPrecision(1)
                                                       .build());
        try (Recommender rec = LenskitRecommender.build(config, dao)) {
            ItemScorer predictor = rec.getItemScorer();

            assertThat(predictor, notNullValue());
            assertEquals(2.6, predictor.score(2, 9).getScore(), EPSILON);
            assertEquals(4.2, predictor.score(3, 6).getScore(), EPSILON);
            assertEquals(2, predictor.score(4, 6).getScore(), EPSILON);
            assertEquals(2, predictor.score(4, 9).getScore(), EPSILON);
            assertEquals(2.5, predictor.score(5, 6).getScore(), EPSILON);
            assertEquals(3, predictor.score(5, 7).getScore(), EPSILON);
            assertEquals(3.5, predictor.score(5, 9).getScore(), EPSILON);
            assertEquals(1.5, predictor.score(6, 6).getScore(), EPSILON);
            assertEquals(2, predictor.score(6, 7).getScore(), EPSILON);
            assertEquals(2.5, predictor.score(6, 9).getScore(), EPSILON);
        }
    }

    @Test
    public void testPredict2() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 4, 3.5));
        rs.add(Rating.create(2, 4, 5));
        rs.add(Rating.create(3, 5, 4.25));
        rs.add(Rating.create(2, 6, 3));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(2, 7, 4));
        rs.add(Rating.create(3, 7, 1.5));

        StaticDataSource source = StaticDataSource.fromList(rs);
        DataAccessObject dao = source.get();

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemScorer.class).to(WeightedSlopeOneItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomainBuilder(1, 5)
                                                       .setPrecision(1)
                                                       .build());
        try (Recommender rec = LenskitRecommender.build(config, dao)) {
            ItemScorer predictor = rec.getItemScorer();

            assertThat(predictor, notNullValue());
            assertEquals(5, predictor.score(1, 5).getScore(), EPSILON);
            assertEquals(2.25, predictor.score(1, 6).getScore(), EPSILON);
            assertEquals(5, predictor.score(2, 5).getScore(), EPSILON);
            assertEquals(1.75, predictor.score(3, 4).getScore(), EPSILON);
            assertEquals(1, predictor.score(3, 6).getScore(), EPSILON);
        }
    }
}
