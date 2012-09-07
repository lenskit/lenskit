/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.svd;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.UserMeanPredictor;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.snapshot.PackedPreferenceSnapshot;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.params.IterationCount;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TestFunkSVDRecommenderBuild {
    private LenskitRecommenderEngine engine;

    @Before
    public void setup() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 5, 2));
        rs.add(Ratings.make(1, 7, 4));
        rs.add(Ratings.make(8, 4, 5));
        rs.add(Ratings.make(8, 5, 4));

        DAOFactory daof = new EventCollectionDAO.Factory(rs);

        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(daof);
        factory.bind(PreferenceSnapshot.class).to(PackedPreferenceSnapshot.class);
        factory.bind(RatingPredictor.class).to(FunkSVDRatingPredictor.class);
        factory.bind(BaselinePredictor.class).to(UserMeanPredictor.class);
        factory.bind(ItemRecommender.class).to(FunkSVDRecommender.class);
        factory.bind(Integer.class).withQualifier(IterationCount.class).to(10);

        engine = factory.create();
    }

    @Test
    public void testFunkSVDRecommenderEngineCreate() {
        Recommender rec = engine.open();

        try {
            assertThat(rec.getRatingPredictor(),
                       instanceOf(FunkSVDRatingPredictor.class));
            assertThat(rec.getItemRecommender(),
                       instanceOf(FunkSVDRecommender.class));
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
            assertThat(rec1.get(FunkSVDModel.class),
                       allOf(not(nullValue()),
                             sameInstance(rec2.get(FunkSVDModel.class))));
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
