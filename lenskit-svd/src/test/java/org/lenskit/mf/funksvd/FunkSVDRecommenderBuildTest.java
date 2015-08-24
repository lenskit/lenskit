/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.mf.funksvd;

import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.snapshot.PackedPreferenceSnapshot;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.IterationCountStoppingCondition;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Recommender;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.baseline.ItemMeanRatingItemScorer;
import org.lenskit.baseline.UserMeanBaseline;
import org.lenskit.baseline.UserMeanItemScorer;
import org.lenskit.basic.SimpleRatingPredictor;
import org.lenskit.basic.TopNItemRecommender;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class FunkSVDRecommenderBuildTest {
    private EventDAO dao;

    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));

        dao = EventCollectionDAO.create(rs);
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    private LenskitRecommenderEngine makeEngine() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        config.bind(PreferenceSnapshot.class)
              .to(PackedPreferenceSnapshot.class);
        config.bind(ItemScorer.class)
              .to(FunkSVDItemScorer.class);
        config.bind(BaselineScorer.class, ItemScorer.class)
              .to(UserMeanItemScorer.class);
        config.bind(UserMeanBaseline.class, ItemScorer.class)
              .to(ItemMeanRatingItemScorer.class);
        config.bind(StoppingCondition.class)
              .to(IterationCountStoppingCondition.class);
        config.set(IterationCount.class)
              .to(10);
        config.set(FeatureCount.class)
              .to(20);

        return LenskitRecommenderEngine.build(config);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Ignore("will not work until moved")
    public void testFunkSVDRecommenderEngineCreate() throws RecommenderBuildException {
        LenskitRecommenderEngine engine = makeEngine();
        Recommender rec = engine.createRecommender();

        assertThat(rec.getItemScorer(),
                   instanceOf(FunkSVDItemScorer.class));
        assertThat(rec.getItemRecommender(),
                   instanceOf(TopNItemRecommender.class));
        RatingPredictor pred = rec.getRatingPredictor();
        assertThat(pred, notNullValue());
        assertThat(pred, instanceOf(SimpleRatingPredictor.class));
        assertThat(((SimpleRatingPredictor) pred).getItemScorer(),
                   sameInstance(rec.getItemScorer()));
    }

    @Test
    public void testFeatureInfo() throws RecommenderBuildException {
        LenskitRecommenderEngine engine = makeEngine();
        LenskitRecommender rec = engine.createRecommender();

        FunkSVDModel model = rec.get(FunkSVDModel.class);
        assertThat(model, notNullValue());
        assertThat(model.getFeatureInfo().size(),
                   equalTo(20));
        for (FeatureInfo feat: model.getFeatureInfo()) {
            assertThat(feat.getIterCount(), equalTo(10));
            assertThat(feat.getLastDeltaRMSE(),
                       greaterThan(0.0));
        }
    }

    @Test
    public void testConfigSeparation() throws RecommenderBuildException {
        LenskitRecommenderEngine engine = makeEngine();
        LenskitRecommender rec1 = null;
        LenskitRecommender rec2 = null;
        rec1 = engine.createRecommender();
        rec2 = engine.createRecommender();

        assertThat(rec1.getItemScorer(),
                   not(sameInstance(rec2.getItemScorer())));
        assertThat(rec1.get(FunkSVDModel.class),
                   sameInstance(rec2.get(FunkSVDModel.class)));
    }
}
