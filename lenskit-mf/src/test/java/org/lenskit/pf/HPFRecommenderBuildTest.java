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
package org.lenskit.pf;

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.StoppingThreshold;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.basic.SimpleRatingPredictor;
import org.lenskit.basic.TopNItemRecommender;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.EntityFactory;
import org.lenskit.data.ratings.PackedRatingMatrix;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingMatrix;
import org.lenskit.mf.funksvd.FeatureCount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class HPFRecommenderBuildTest {
    private DataAccessObject dao;
    private Long2ObjectMap<Long2DoubleMap> data;

    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        double[][] ratingsImplicit = {
                {0, 1, 1, 1, 1},
                {1, 0, 1, 0, 0},
                {1, 1, 1, 1, 0},
                {1, 1, 0, 0, 1},
                {1, 1, 1, 1, 1},
                {0, 0, 0, 1, 0}};
        double[][] ratings = {
                {0, 5, 2, 10, 1},
                {1, 0, 10, 0, 0},
                {1, 1, 8, 10, 0},
                {1, 8, 0, 0, 1},
                {1, 2, 2, 5, 3},
                {0, 0, 0, 6, 0}};
        EntityFactory ef = new EntityFactory();
        data = new Long2ObjectOpenHashMap<>();
        for (int user = 1; user <= ratings.length; user++) {
            double[] userRatings = ratings[user-1];
            for (int item = 1; item <= userRatings.length; item++) {
                double rating = userRatings[item-1];
                rs.add(ef.rating(user, item, rating));
                Long2DoubleMap itemRatings = data.get(item);
                if (itemRatings == null) itemRatings = new Long2DoubleOpenHashMap();
                itemRatings.put(user, rating);
                data.put(item, itemRatings);
            }
        }

        StaticDataSource source = StaticDataSource.fromList(rs);
        dao = source.get();
    }

    private LenskitRecommenderEngine makeEngine() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(RatingMatrix.class)
                .to(PackedRatingMatrix.class);
        config.bind(ItemScorer.class)
                .to(HPFItemScorer.class);
        config.bind(HPFModel.class)
                .toProvider(HPFModelProvider.class);
        config.set(ConvergenceCheckFrequency.class)
                .to(2);
        config.set(StoppingThreshold.class)
                .to(0.000001);
        config.set(FeatureCount.class)
                .to(5);
        config.set(SplitProportion.class)
                .to(0.1);
//        config.set(RandomSeed.class)
//                .to(System.currentTimeMillis());
        config.set(IterationCount.class)
                .to(1000);
        config.set(IsProbabilityPrediction.class)
                .to(false);

        return LenskitRecommenderEngine.build(config, dao);
    }

    @Test
    public void testHPFRecommenderEngineCreate() throws RecommenderBuildException {
        LenskitRecommenderEngine engine = makeEngine();
        try (Recommender rec = engine.createRecommender(dao)) {

            assertThat(rec.getItemScorer(),
                    instanceOf(HPFItemScorer.class));
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
    public void testConfigSeparation() throws RecommenderBuildException {
        LenskitRecommenderEngine engine = makeEngine();
        try (LenskitRecommender rec1 = engine.createRecommender(dao);
             LenskitRecommender rec2 = engine.createRecommender(dao)) {
            assertThat(rec1.getItemScorer(),
                    not(sameInstance(rec2.getItemScorer())));
            assertThat(rec1.get(HPFModel.class), sameInstance(rec2.get(HPFModel.class)));
        }
    }

    @Test
    public void testHPFRecommenderScorer() throws RecommenderBuildException {
        LenskitRecommenderEngine engine = makeEngine();
        LongArrayList items = new LongArrayList();
        long[] itemIds = {1L, 2L, 3L, 4L, 5L};
        items.addElements(0, itemIds);
        try (Recommender rec = engine.createRecommender(dao)) {
            Map<Long,Double> results = rec.getItemScorer().score(1L,items);
            assertThat(results, notNullValue());
            System.out.println(results);
            System.out.println(rec.getItemScorer().score(2L,items));
            System.out.println(rec.getItemScorer().score(3L,items));
            System.out.println(rec.getItemScorer().score(4L,items));
            System.out.println(rec.getItemScorer().score(5L,items));
            System.out.println(rec.getItemScorer().score(6L,items));
        }
    }

}
