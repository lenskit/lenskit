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
package org.grouplens.lenskit.slopeone;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TestSlopeOneItemRecommender {
    private LenskitRecommenderEngine engine;

    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 5, 2));
        rs.add(Ratings.make(1, 7, 4));
        rs.add(Ratings.make(8, 4, 5));
        rs.add(Ratings.make(8, 5, 4));

        DAOFactory daof = new EventCollectionDAO.Factory(rs);

        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(daof);
        factory.bind(RatingPredictor.class).to(SlopeOneRatingPredictor.class);
        factory.bind(ItemRecommender.class).to(SlopeOneRecommender.class);
        factory.bind(PreferenceDomain.class).to(new PreferenceDomain(1, 5));
        // factory.setComponent(UserVectorNormalizer.class, IdentityVectorNormalizer.class);
        factory.bind(BaselinePredictor.class).to(ItemUserMeanPredictor.class);
        engine = factory.create();
    }

    @Test
    public void testSlopeOneRecommenderEngineCreate() {
        Recommender rec = engine.open();

        try {
            assertThat(rec.getItemScorer(),
                       instanceOf(SlopeOneRatingPredictor.class));
            assertThat(rec.getRatingPredictor(),
                       instanceOf(SlopeOneRatingPredictor.class));
            assertThat(rec.getItemRecommender(),
                       instanceOf(SlopeOneRecommender.class));
        } finally {
            rec.close();
        }
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
            assertThat(rec1.get(SlopeOneModel.class),
                       allOf(not(nullValue()),
                             sameInstance(rec2.get(SlopeOneModel.class))));
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
