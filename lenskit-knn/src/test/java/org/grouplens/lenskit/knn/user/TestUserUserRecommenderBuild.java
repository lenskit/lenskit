package org.grouplens.lenskit.knn.user;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.grouplens.lenskit.DynamicRatingItemRecommender;
import org.grouplens.lenskit.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.dao.DataAccessObjectManager;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.norm.IdentityUserRatingVectorNormalizer;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;
import org.junit.Before;
import org.junit.Test;

public class TestUserUserRecommenderBuild {

	private DataAccessObjectManager<? extends RatingDataAccessObject> manager;
	private static RecommenderEngine engine;

	@Before
	public void setup() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 5, 2));
		rs.add(new SimpleRating(1, 7, 4));
		rs.add(new SimpleRating(8, 4, 5));
		rs.add(new SimpleRating(8, 5, 4));

		manager = new RatingCollectionDAO.Manager(rs);

		LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(manager);
		factory.setComponent(RatingPredictor.class, UserUserRatingPredictor.class);
		factory.setComponent(DynamicRatingItemRecommender.class, UserUserRatingRecommender.class);
		factory.setComponent(NeighborhoodFinder.class, SimpleNeighborhoodFinder.class);
		factory.setComponent(UserRatingVectorNormalizer.class, IdentityUserRatingVectorNormalizer.class);

		engine = factory.create();
	}

	@Test
	public void testUserUserRecommenderEngineCreate() {        
		Recommender rec = engine.open();

		try {
			// These assert instanceof's are also assertNotNull's
			Assert.assertTrue(rec.getDynamicRatingPredictor() instanceof UserUserRatingPredictor);
			Assert.assertTrue(rec.getRatingPredictor() instanceof UserUserRatingPredictor);
			Assert.assertTrue(rec.getDynamicItemRecommender() instanceof UserUserRatingRecommender);
			Assert.assertNull(rec.getBasketRecommender());
		} finally {
			rec.close();
		}
	}
}
