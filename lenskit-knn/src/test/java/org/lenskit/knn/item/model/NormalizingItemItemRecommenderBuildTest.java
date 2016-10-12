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
package org.lenskit.knn.item.model;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemBasedItemScorer;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.basic.SimpleRatingPredictor;
import org.lenskit.basic.TopNItemBasedItemRecommender;
import org.lenskit.basic.TopNItemRecommender;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;
import org.lenskit.knn.item.ItemItemItemBasedItemScorer;
import org.lenskit.knn.item.ItemItemScorer;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class NormalizingItemItemRecommenderBuildTest {
    private LenskitRecommenderEngine engine;
    private DataAccessObject dao;

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
        config.bind(ItemItemModel.class).toProvider(NormalizingItemItemModelProvider.class);
        config.bind(ItemScorer.class).to(ItemItemScorer.class);
        config.bind(ItemBasedItemScorer.class).to(ItemItemItemBasedItemScorer.class);
        // this is the default
//        factory.setComponent(UserVectorNormalizer.class, VectorNormalizer.class,
//                             IdentityVectorNormalizer.class);

        engine = LenskitRecommenderEngine.build(config, dao);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testItemItemRecommenderEngineCreate() {
        try (LenskitRecommender rec = engine.createRecommender(dao)) {

            assertThat(rec.getItemScorer(),
                       instanceOf(ItemItemScorer.class));
            assertThat(rec.getRatingPredictor(),
                       instanceOf(SimpleRatingPredictor.class));
            assertThat(rec.getItemRecommender(),
                       instanceOf(TopNItemRecommender.class));
            assertThat(rec.getItemBasedItemRecommender(),
                       instanceOf(TopNItemBasedItemRecommender.class));
            assertThat(rec.get(ItemBasedItemScorer.class),
                       instanceOf(ItemItemItemBasedItemScorer.class));
        }
    }

    @Test
    public void testConfigSeparation() {
        try(LenskitRecommender rec1 = engine.createRecommender(dao);
            LenskitRecommender rec2 = engine.createRecommender(dao)){

            assertThat(rec1.getItemScorer(),
                       not(sameInstance(rec2.getItemScorer())));
            assertThat(rec1.get(ItemItemModel.class),
                       allOf(not(nullValue()),
                             sameInstance(rec2.get(ItemItemModel.class))));
        }
    }
}
