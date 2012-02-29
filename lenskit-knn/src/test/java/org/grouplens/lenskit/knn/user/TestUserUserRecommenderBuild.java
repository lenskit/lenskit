/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.junit.Before;
import org.junit.Test;

public class TestUserUserRecommenderBuild {

    private DAOFactory manager;
    private static RecommenderEngine engine;

    @Before
    public void setup() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 5, 2));
        rs.add(Ratings.make(1, 7, 4));
        rs.add(Ratings.make(8, 4, 5));
        rs.add(Ratings.make(8, 5, 4));

        manager = new EventCollectionDAO.Factory(rs);

        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(manager);
        factory.setComponent(RatingPredictor.class, UserUserRatingPredictor.class);
        factory.setComponent(ItemRecommender.class, UserUserRecommender.class);
        factory.setComponent(NeighborhoodFinder.class, SimpleNeighborhoodFinder.class);

        engine = factory.create();
    }

    @Test
    public void testUserUserRecommenderEngineCreate() {
        Recommender rec = engine.open();

        try {
            // These assert instanceof's are also assertNotNull's
            Assert.assertTrue(rec.getRatingPredictor() instanceof UserUserRatingPredictor);
            Assert.assertTrue(rec.getRatingPredictor() instanceof UserUserRatingPredictor);
            Assert.assertTrue(rec.getItemRecommender() instanceof UserUserRecommender);
        } finally {
            rec.close();
        }
    }
}
