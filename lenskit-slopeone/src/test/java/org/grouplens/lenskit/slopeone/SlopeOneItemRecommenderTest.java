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

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer;
import org.grouplens.lenskit.baseline.UserMeanBaseline;
import org.grouplens.lenskit.baseline.UserMeanItemScorer;
import org.grouplens.lenskit.basic.SimpleRatingPredictor;
import org.grouplens.lenskit.basic.TopNItemRecommender;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class SlopeOneItemRecommenderTest {
    private LenskitRecommenderEngine engine;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 5, 2));
        rs.add(Ratings.make(1, 7, 4));
        rs.add(Ratings.make(8, 4, 5));
        rs.add(Ratings.make(8, 5, 4));

        EventDAO dao = new EventCollectionDAO(rs);

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        config.bind(ItemScorer.class).to(SlopeOneItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomain(1, 5));
        // factory.setComponent(UserVectorNormalizer.class, IdentityVectorNormalizer.class);
        config.bind(BaselineScorer.class, ItemScorer.class)
              .to(UserMeanItemScorer.class);
        config.bind(UserMeanBaseline.class, ItemScorer.class)
              .to(ItemMeanRatingItemScorer.class);
        engine = LenskitRecommenderEngine.build(config);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSlopeOneRecommenderEngineCreate() {
        Recommender rec = engine.createRecommender();

        assertThat(rec.getItemScorer(),
                   instanceOf(SlopeOneItemScorer.class));
        RatingPredictor rp = rec.getRatingPredictor();
        assertThat(rp, instanceOf(SimpleRatingPredictor.class));
        assertThat(((SimpleRatingPredictor) rp).getScorer(),
                   sameInstance(rec.getItemScorer()));
        assertThat(rec.getItemRecommender(),
                   instanceOf(TopNItemRecommender.class));
    }

    @Test
    public void testConfigSeparation() {
        LenskitRecommender rec1 = null;
        LenskitRecommender rec2 = null;
        rec1 = engine.createRecommender();
        rec2 = engine.createRecommender();

        assertThat(rec1.getItemScorer(),
                   not(sameInstance(rec2.getItemScorer())));
        assertThat(rec1.get(SlopeOneModel.class),
                   allOf(not(nullValue()),
                         sameInstance(rec2.get(SlopeOneModel.class))));
    }
}
