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
package org.lenskit.knn.item;

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
import org.lenskit.knn.item.model.ItemItemBuildContext;
import org.lenskit.knn.item.model.ItemItemModel;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ItemItemRecommenderBuildTest {

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

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(DataAccessObject.class).toProvider(source);
        config.bind(ItemScorer.class).to(ItemItemScorer.class);
        config.bind(ItemBasedItemScorer.class).to(ItemItemItemBasedItemScorer.class);
        // this is the default
//        factory.setComponent(UserVectorNormalizer.class, VectorNormalizer.class,
//                             IdentityVectorNormalizer.class);

        engine = LenskitRecommenderEngine.build(config);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testItemItemRecommenderEngineCreate() {
        try (LenskitRecommender rec = engine.createRecommender()) {

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
    public void testContextRemoved() {
        try (LenskitRecommender rec = engine.createRecommender()) {
            assertThat(rec.get(ItemItemBuildContext.class),
                       nullValue());
        }
    }

    @Test
    public void testConfigSeparation() {
        try (LenskitRecommender rec1 = engine.createRecommender();
             LenskitRecommender rec2 = engine.createRecommender()) {

            assertThat(rec1.getItemScorer(),
                       not(sameInstance(rec2.getItemScorer())));
            assertThat(rec1.get(ItemItemModel.class),
                       allOf(not(nullValue()),
                             sameInstance(rec2.get(ItemItemModel.class))));
        }
    }
}
