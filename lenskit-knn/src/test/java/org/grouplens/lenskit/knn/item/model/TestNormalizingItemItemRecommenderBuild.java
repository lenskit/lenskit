/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.knn.item.model;

import org.grouplens.lenskit.*;
import org.grouplens.lenskit.basic.SimpleRatingPredictor;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.knn.item.ItemItemGlobalRecommender;
import org.grouplens.lenskit.knn.item.ItemItemGlobalScorer;
import org.grouplens.lenskit.knn.item.ItemItemRecommender;
import org.grouplens.lenskit.knn.item.ItemItemScorer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TestNormalizingItemItemRecommenderBuild {
    private LenskitRecommenderEngine engine;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 5, 2));
        rs.add(Ratings.make(1, 7, 4));
        rs.add(Ratings.make(8, 4, 5));
        rs.add(Ratings.make(8, 5, 4));
        DAOFactory daof = new EventCollectionDAO.Factory(rs);

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemItemModel.class).toProvider(NormalizingItemItemModelBuilder.class);
        config.bind(ItemScorer.class).to(ItemItemScorer.class);
        config.bind(ItemRecommender.class).to(ItemItemRecommender.class);
        config.bind(GlobalItemRecommender.class).to(ItemItemGlobalRecommender.class);
        config.bind(GlobalItemScorer.class).to(ItemItemGlobalScorer.class);
        // this is the default
//        factory.setComponent(UserVectorNormalizer.class, VectorNormalizer.class,
//                             IdentityVectorNormalizer.class);

        engine = LenskitRecommenderEngine.build(daof, config);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testItemItemRecommenderEngineCreate() {
        Recommender rec = engine.open();

        assertThat(rec.getItemScorer(),
                instanceOf(ItemItemScorer.class));
        assertThat(rec.getRatingPredictor(),
                instanceOf(SimpleRatingPredictor.class));
        assertThat(rec.getItemRecommender(),
                instanceOf(ItemItemRecommender.class));
        assertThat(rec.getGlobalItemRecommender(),
                instanceOf(ItemItemGlobalRecommender.class));
        assertThat(rec.getGlobalItemScorer(),
                instanceOf(ItemItemGlobalScorer.class));
    }

    @Test
    public void testConfigSeparation() {
        LenskitRecommender rec1 = null;
        LenskitRecommender rec2 = null;
        try {
            rec1 = engine.open();
            rec2 = engine.open();

            assertThat(rec1.getItemScorer(),
                    not(sameInstance(rec2.getItemScorer())));
            assertThat(rec1.get(ItemItemModel.class),
                    allOf(not(nullValue()),
                            sameInstance(rec2.get(ItemItemModel.class))));
        } finally {
            if (rec2 != null) {
                rec2.close();
            }
            if (rec1 != null) {
                rec1.close();
            }
        }
    }
}
