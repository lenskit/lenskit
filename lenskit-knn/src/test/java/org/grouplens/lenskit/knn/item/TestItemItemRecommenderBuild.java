package org.grouplens.lenskit.knn.item;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

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
import org.grouplens.lenskit.knn.SimilarityMatrixAccumulatorFactory;
import org.grouplens.lenskit.knn.TruncatingSimilarityMatrixAccumulator;
import org.grouplens.lenskit.norm.IdentityUserRatingVectorNormalizer;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;
import org.junit.Before;
import org.junit.Test;

public class TestItemItemRecommenderBuild {

	private DataAccessObjectManager<? extends RatingDataAccessObject> manager;
	private RecommenderEngine engine;

	@Before
	public void setup() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 5, 2));
		rs.add(new SimpleRating(1, 7, 4));
		rs.add(new SimpleRating(8, 4, 5));
		rs.add(new SimpleRating(8, 5, 4));
		manager = new RatingCollectionDAO.Manager(rs);

		LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(manager);
		factory.setComponent(RatingPredictor.class, ItemItemRatingPredictor.class);
		factory.setComponent(DynamicRatingItemRecommender.class, ItemItemRatingRecommender.class);
		factory.setComponent(SimilarityMatrixAccumulatorFactory.class, 
				TruncatingSimilarityMatrixAccumulator.Factory.class);
		factory.setComponent(UserRatingVectorNormalizer.class, IdentityUserRatingVectorNormalizer.class);

		engine = factory.create();
	}

	@Test
	public void testItemItemRecommenderEngineCreate() {
		Recommender rec = engine.open();
		
		// These assert instanceof's are also assertNotNull's
		assertTrue(rec.getDynamicRatingPredictor() instanceof ItemItemRatingPredictor);
		assertTrue(rec.getRatingPredictor() instanceof ItemItemRatingPredictor);
		assertTrue(rec.getDynamicRatingItemRecommender() instanceof ItemItemRatingRecommender);
		assertNull(rec.getBasketRecommender());
	}
}
