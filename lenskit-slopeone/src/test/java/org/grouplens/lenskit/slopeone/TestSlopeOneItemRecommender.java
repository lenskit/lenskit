/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.slopeone;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.norm.IdentityUserRatingVectorNormalizer;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;
import org.junit.Before;
import org.junit.Test;

public class TestSlopeOneItemRecommender {
    private DAOFactory<? extends RatingDataAccessObject> manager;
    private RecommenderEngine engine;
    
    @Before
    public void setup() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(new SimpleRating(1, 5, 2));
        rs.add(new SimpleRating(1, 7, 4));
        rs.add(new SimpleRating(8, 4, 5));
        rs.add(new SimpleRating(8, 5, 4));
        
        manager = new RatingCollectionDAO.Factory(rs);
        
        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(manager);
        factory.setComponent(RatingPredictor.class, SlopeOneRatingPredictor.class);
        factory.setComponent(ItemRecommender.class, SlopeOneItemRecommender.class);
        factory.setComponent(UserRatingVectorNormalizer.class, IdentityUserRatingVectorNormalizer.class);
        factory.setComponent(BaselinePredictor.class, ItemUserMeanPredictor.class);
        engine = factory.create();
    }
    
    @Test
    public void testSlopeOneRecommenderEngineCreate() {
        Recommender rec = engine.open();
        
        try {
            // These assert instanceof's are also assertNotNull's
            Assert.assertTrue(rec.getDynamicRatingPredictor() instanceof SlopeOneRatingPredictor);
            Assert.assertTrue(rec.getRatingPredictor() instanceof SlopeOneRatingPredictor);
            Assert.assertTrue(rec.getItemRecommender() instanceof SlopeOneItemRecommender);
        } finally {
            rec.close();
        }
    }
}
