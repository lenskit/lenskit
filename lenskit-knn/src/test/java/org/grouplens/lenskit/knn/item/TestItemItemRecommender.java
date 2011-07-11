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
package org.grouplens.lenskit.knn.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.DynamicRatingItemRecommender;
import org.grouplens.lenskit.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.grouplens.lenskit.data.vector.UserRatingVector;
import org.grouplens.lenskit.knn.SimilarityMatrixAccumulatorFactory;
import org.grouplens.lenskit.knn.TruncatingSimilarityMatrixAccumulator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestItemItemRecommender {

	private static Recommender itemItemRecommender;
	private static DynamicRatingItemRecommender recommender;
	private static RatingDataAccessObject dao;

	@BeforeClass
	public static void setup() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 6, 4));
		rs.add(new SimpleRating(2, 6, 2));
		rs.add(new SimpleRating(1, 7, 3));
		rs.add(new SimpleRating(2, 7, 2));
		rs.add(new SimpleRating(3, 7, 5));
		rs.add(new SimpleRating(4, 7, 2));
		rs.add(new SimpleRating(1, 8, 3));
		rs.add(new SimpleRating(2, 8, 4));
		rs.add(new SimpleRating(3, 8, 3));
		rs.add(new SimpleRating(4, 8, 2));
		rs.add(new SimpleRating(5, 8, 3));
		rs.add(new SimpleRating(6, 8, 2));
		rs.add(new SimpleRating(1, 9, 3));
		rs.add(new SimpleRating(3, 9, 4));
		RatingCollectionDAO.Factory manager = new RatingCollectionDAO.Factory(rs);
		LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(manager);
		factory.setComponent(RatingPredictor.class, ItemItemRatingPredictor.class);
		factory.setComponent(DynamicRatingItemRecommender.class, ItemItemRecommender.class);
		factory.setComponent(SimilarityMatrixAccumulatorFactory.class, 
				TruncatingSimilarityMatrixAccumulator.Factory.class);
		// this is the default
		// FIXME Let this work @mludwig
		/*factory.setComponent(UserRatingVectorNormalizer.class, VectorNormalizer.class,
		                     IdentityVectorNormalizer.class);*/
		RecommenderEngine engine = factory.create();
		itemItemRecommender = engine.open();
		recommender = itemItemRecommender.getDynamicItemRecommender();
		dao = manager.create();
	}



	/**
	 * Tests <tt>recommend(long, SparseVector)</tt>.
	 */
	@Test
	public void testItemItemRecommender1() {
		LongList recs = recommender.recommend(getRatingVector(1));
		assertTrue(recs.isEmpty());
		
		recs = recommender.recommend(getRatingVector(2));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));
		
		recs = recommender.recommend(getRatingVector(3));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(6));
		
		recs = recommender.recommend(getRatingVector(4));
		assertEquals(2, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(9));
		
		recs = recommender.recommend(getRatingVector(5));
		assertEquals(3, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));
		assertTrue(recs.contains(9));
		
		recs = recommender.recommend(getRatingVector(6));
		assertEquals(3, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));
		assertTrue(recs.contains(9));
	}

	/**
	 * Tests <tt>recommend(long, SparseVector, int)</tt>.
	 */
	@Test
	public void testItemItemRecommender2() {
		LongList recs = recommender.recommend(getRatingVector(2), 1);
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));
		
		recs = recommender.recommend(getRatingVector(2), 0);
		assertTrue(recs.isEmpty());	
		
		recs = recommender.recommend(getRatingVector(3), 1);
		assertTrue(recs.contains(6) || recs.contains(9));
		
		recs = recommender.recommend(getRatingVector(4), 0);
		assertTrue(recs.isEmpty());
	}

	/**
	 * Tests <tt>recommend(long, SparseVector, Set)</tt>.
	 */
	@Test
	public void testItemItemRecommender3() {
		LongList recs = recommender.recommend(getRatingVector(1), null);
		assertTrue(recs.isEmpty());
		
		
		LongOpenHashSet candidates = new LongOpenHashSet();
		candidates.add(6);
		candidates.add(7);
		candidates.add(8);
		candidates.add(9);
		recs = recommender.recommend(getRatingVector(1), candidates);
		assertTrue(recs.isEmpty());
		
		recs = recommender.recommend(getRatingVector(2), null);
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));
		
		candidates.clear();
		candidates.add(7);
		candidates.add(8);
		candidates.add(9);
		recs = recommender.recommend(getRatingVector(2), candidates);
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));
		
		candidates.add(6);
		candidates.remove(9);
		recs = recommender.recommend(getRatingVector(2), candidates);
		assertTrue(recs.isEmpty());
		
		recs = recommender.recommend(getRatingVector(5), null);
		assertEquals(3, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));
		assertTrue(recs.contains(9));
		
		candidates.clear();
		candidates.add(6);
		candidates.add(7);
		recs = recommender.recommend(getRatingVector(5), candidates);
		assertEquals(2, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));
		
		candidates.clear();
		candidates.add(6);
		candidates.add(9);
		recs = recommender.recommend(getRatingVector(5), candidates);
		assertEquals(2, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(9));
	}

	/**
	 * Tests <tt>recommend(long, SparseVector, int, Set, Set)</tt>.
	 */
	@Test
	public void testItemItemRecommender4() {
		LongList recs = recommender.recommend(getRatingVector(5), -1, null, null);
		assertEquals(3, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));
		assertTrue(recs.contains(9));
		
		LongOpenHashSet candidates = new LongOpenHashSet();
		candidates.add(6);
		candidates.add(7);
		candidates.add(8);
		candidates.add(9);
		recs = recommender.recommend(getRatingVector(5), -1, candidates, null);
		assertEquals(3, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));
		assertTrue(recs.contains(9));
		
		candidates.remove(6);
		recs = recommender.recommend(getRatingVector(5), -1, candidates, null);
		assertEquals(2, recs.size());
		assertTrue(recs.contains(7));
		assertTrue(recs.contains(9));
		
		candidates.remove(7);
		recs = recommender.recommend(getRatingVector(5), -1, candidates, null);
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));
		
		candidates.remove(9);
		recs = recommender.recommend(getRatingVector(5), -1, candidates, null);
		assertTrue(recs.isEmpty());
		
		candidates.add(9);
		candidates.add(7);
		recs = recommender.recommend(getRatingVector(5), 1, candidates, null);
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9) || recs.contains(7));
		
		LongOpenHashSet exclude = new LongOpenHashSet();
		exclude.add(7);
		recs = recommender.recommend(getRatingVector(5), 2, candidates, exclude);
		assertEquals(1,recs.size());
		assertTrue(recs.contains(9));
		
		recs = recommender.recommend(getRatingVector(5), 0, candidates, null);
		assertTrue(recs.isEmpty());
		
		candidates.clear();
		candidates.add(7);
		candidates.add(9);
		recs = recommender.recommend(getRatingVector(5), -1, candidates, null);
		assertEquals(2, recs.size());
		assertTrue(recs.contains(7));
		assertTrue(recs.contains(9));
		
		candidates.add(6);
		exclude.clear();
		exclude.add(9);
		recs = recommender.recommend(getRatingVector(5), -1, candidates, exclude);
		assertEquals(2, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));
		
		exclude.add(7);
		recs = recommender.recommend(getRatingVector(5), -1, candidates, exclude);
		assertEquals(1, recs.size());
		assertTrue(recs.contains(6));
		
		exclude.add(6);
		recs = recommender.recommend(getRatingVector(5), -1, candidates, exclude);
		assertTrue(recs.isEmpty());
	}
	
	//Helper method to retrieve user's user and create SparseVector
	private static UserRatingVector getRatingVector(long user) {
		return UserRatingVector.fromRatings(user, dao.getUserRatings(user));
	}
	
	@AfterClass
	public static void cleanUp() {
		itemItemRecommender.close();
		dao.close();
	}
}
