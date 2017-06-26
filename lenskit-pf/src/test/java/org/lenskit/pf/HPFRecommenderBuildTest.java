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
package org.lenskit.pf;

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.iterative.*;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.*;
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
        double[][] ratings = {
                {0, 1, 1, 1, 1},
                {1, 0, 1, 0, 0},
                {1, 1, 1, 1, 0},
                {1, 1, 0, 0, 1},
                {1, 1, 1, 1, 1},
                {0, 0, 0, 1, 0}};
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
//        config.bind(StoppingCondition.class)
//                .to(IterationCountStoppingCondition.class);
//        config.set(IterationCount.class)
//                .to(10);
        config.bind(StoppingCondition.class)
                .to(ErrorThresholdStoppingCondition.class);
        config.set(IterationFrequency.class)
                .to(10);
        config.set(MinimumIterations.class)
                .to(20);
        config.set(StoppingThreshold.class)
                .to(0.000001);
        config.set(FeatureCount.class)
                .to(5);

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
