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
package org.lenskit.knn.user;

import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.basic.TopNItemRecommender;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderEngine;
import org.lenskit.basic.SimpleRatingPredictor;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UserUserRecommenderBuildTest {

    private static RecommenderEngine engine;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));

        EventDAO dao = new EventCollectionDAO(rs);

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        config.bind(ItemScorer.class).to(UserUserItemScorer.class);
        config.bind(NeighborFinder.class).to(LiveNeighborFinder.class);

        engine = LenskitRecommenderEngine.build(config);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Ignore("will not work until moved")
    public void testUserUserRecommenderEngineCreate() {
        Recommender rec = engine.createRecommender();

        assertThat(rec.getItemScorer(),
                   instanceOf(UserUserItemScorer.class));
        assertThat(rec.getItemRecommender(),
                   instanceOf(TopNItemRecommender.class));
        RatingPredictor pred = rec.getRatingPredictor();
        assertThat(pred, notNullValue());
        assertThat(pred, instanceOf(SimpleRatingPredictor.class));
        assertThat(((SimpleRatingPredictor) pred).getItemScorer(),
                   sameInstance(rec.getItemScorer()));
    }

    @Ignore("will not work until moved")
    @Test
    public void testSnapshot() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 5, 2));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(8, 4, 5));
        rs.add(Rating.create(8, 5, 4));

        EventDAO dao = EventCollectionDAO.create(rs);

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        config.bind(ItemScorer.class).to(UserUserItemScorer.class);
        config.bind(NeighborFinder.class).to(SnapshotNeighborFinder.class);

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config);
        Recommender rec = engine.createRecommender();
        assertThat(rec.getItemScorer(),
                   instanceOf(UserUserItemScorer.class));
        assertThat(rec.getItemRecommender(),
                   instanceOf(TopNItemRecommender.class));
        RatingPredictor pred = rec.getRatingPredictor();
        assertThat(pred, instanceOf(SimpleRatingPredictor.class));

        Recommender rec2 = engine.createRecommender();
        assertThat(rec2.getItemScorer(), not(sameInstance(rec.getItemScorer())));
    }
}
