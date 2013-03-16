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
package org.grouplens.lenskit.knn.user;

import org.grouplens.lenskit.*;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class TestUserUserRecommenderBuild {

    private static RecommenderEngine engine;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 5, 2));
        rs.add(Ratings.make(1, 7, 4));
        rs.add(Ratings.make(8, 4, 5));
        rs.add(Ratings.make(8, 5, 4));

        DAOFactory daof = new EventCollectionDAO.Factory(rs);

        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(daof);
        factory.bind(RatingPredictor.class).to(UserUserRatingPredictor.class);
        factory.bind(ItemRecommender.class).to(UserUserRecommender.class);
        factory.bind(NeighborhoodFinder.class).to(SimpleNeighborhoodFinder.class);

        engine = factory.create();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUserUserRecommenderEngineCreate() {
        Recommender rec = engine.open();

        try {
            assertThat(rec.getRatingPredictor(),
                       instanceOf(UserUserRatingPredictor.class));
            assertThat(rec.getItemScorer(),
                       instanceOf(UserUserRatingPredictor.class));
            assertThat(rec.getItemRecommender(),
                       instanceOf(UserUserRecommender.class));
        } finally {
            rec.close();
        }
    }
}
