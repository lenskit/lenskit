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
package org.grouplens.lenskit.knn.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.DynamicRatingItemRecommender;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.norm.IdentityUserRatingVectorNormalizer;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

public class TestUserUserRecommender {
	private static Recommender rec;
	private static RatingCollectionDAO dao;

	@BeforeClass
	public static void setup() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 6, 4));
		rs.add(new SimpleRating(2, 6, 2));
		rs.add(new SimpleRating(4, 6, 3));
		rs.add(new SimpleRating(5, 6, 4));
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
		rs.add(new SimpleRating(6, 9, 4));
		rs.add(new SimpleRating(5, 9, 4));
		RatingCollectionDAO.Factory manager = new RatingCollectionDAO.Factory(rs);
		LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(manager);
		factory.setComponent(RatingPredictor.class, UserUserRatingPredictor.class);
		factory.setComponent(DynamicRatingItemRecommender.class, UserUserRatingRecommender.class);
		factory.setComponent(NeighborhoodFinder.class, SimpleNeighborhoodFinder.class);
		factory.setComponent(UserRatingVectorNormalizer.class, IdentityUserRatingVectorNormalizer.class);
		RecommenderEngine engine = factory.create();
		rec = engine.open();
		dao = manager.create();
	}

	/**
	 * Tests <tt>recommend(long, SparseVector)</tt>.
	 */
	@Test
	public void testUserUserRecommender1() {
		DynamicRatingItemRecommender recommender = rec.getDynamicItemRecommender();
		LongList recs = extractIds(recommender.recommend(1, getRatingVector(1)));
		assertTrue(recs.isEmpty());

		recs = extractIds(recommender.recommend(2, getRatingVector(2)));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));

		recs = extractIds(recommender.recommend(3, getRatingVector(3)));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(6));

		recs = extractIds(recommender.recommend(4, getRatingVector(4)));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));

		recs = extractIds(recommender.recommend(5, getRatingVector(5)));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(7));

		recs = extractIds(recommender.recommend(6, getRatingVector(6)));
		assertEquals(2, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));
	}

	/**
	 * Tests <tt>recommend(long, SparseVector, int)</tt>.
	 */
	@Test
	public void testUserUserRecommender2() {
		DynamicRatingItemRecommender recommender = rec.getDynamicItemRecommender();
		LongList recs = extractIds(recommender.recommend(1, getRatingVector(1), -1));
		assertTrue(recs.isEmpty());

		recs = extractIds(recommender.recommend(2, getRatingVector(2), 2));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));

		recs = extractIds(recommender.recommend(2, getRatingVector(2), -1));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));

		recs = extractIds(recommender.recommend(2, getRatingVector(2), 1));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));

		recs = extractIds(recommender.recommend(2, getRatingVector(2), 0));
		assertTrue(recs.isEmpty());

		recs = extractIds(recommender.recommend(3, getRatingVector(3), 1));
		assertEquals(1, recs.size());
		assertEquals(6, recs.getLong(0));

		recs = extractIds(recommender.recommend(3, getRatingVector(3), 0));
		assertTrue(recs.isEmpty());

		recs = extractIds(recommender.recommend(4, getRatingVector(4), 1));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));

		recs = extractIds(recommender.recommend(5, getRatingVector(5), -1));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(7));

		recs = extractIds(recommender.recommend(6, getRatingVector(6), 2));
		assertEquals(2, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));

		recs = extractIds(recommender.recommend(6, getRatingVector(6), 1));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(6) || recs.contains(7));

		recs = extractIds(recommender.recommend(6, getRatingVector(6), 0));
		assertTrue(recs.isEmpty());
	}

	/**
	 * Tests <tt>recommend(long, SparseVector, Set)</tt>.
	 */
	@Test
	public void testUserUserRecommender3() {
		DynamicRatingItemRecommender recommender = rec.getDynamicItemRecommender();
		
		LongOpenHashSet candidates = new LongOpenHashSet();
		candidates.add(6);
		candidates.add(7);
		candidates.add(8);
		candidates.add(9);

		LongList recs = extractIds(recommender.recommend(1, getRatingVector(1), candidates));
		assertTrue(recs.isEmpty());

		candidates.clear();
		candidates.add(9);
		recs = extractIds(recommender.recommend(2, getRatingVector(2), candidates));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));

		candidates.clear();
		candidates.add(6);
		candidates.add(7);
		candidates.add(8);
		recs = extractIds(recommender.recommend(2, getRatingVector(2), candidates));
		assertTrue(recs.isEmpty());

		candidates.add(9);
		recs = extractIds(recommender.recommend(3, getRatingVector(3), candidates));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(6));

		recs = extractIds(recommender.recommend(4, getRatingVector(4), candidates));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));

		recs = extractIds(recommender.recommend(5, getRatingVector(5), candidates));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(7));

		candidates.remove(7);
		recs = extractIds(recommender.recommend(5, getRatingVector(5), candidates));
		assertTrue(recs.isEmpty());

		candidates.add(7);
		recs = extractIds(recommender.recommend(6, getRatingVector(6), candidates));
		assertEquals(2, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));

		candidates.remove(9);
		recs = extractIds(recommender.recommend(6, getRatingVector(6), candidates));
		assertEquals(2, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));

		candidates.remove(8);
		recs = extractIds(recommender.recommend(6, getRatingVector(6), candidates));
		assertEquals(2, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));

		candidates.remove(7);
		recs = extractIds(recommender.recommend(6, getRatingVector(6), candidates));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(6));

		candidates.remove(6);
		recs = extractIds(recommender.recommend(6, getRatingVector(6), candidates));
		assertTrue(recs.isEmpty());
	}

	/**
	 * Tests <tt>recommend(long, SparseVector, int, Set, Set)</tt>.
	 */
	@Test
	public void testUserUserRecommender4() {
		DynamicRatingItemRecommender recommender = rec.getDynamicItemRecommender();
		
		LongOpenHashSet candidates = new LongOpenHashSet();
		candidates.add(9);
		LongList recs = extractIds(recommender.recommend(2, getRatingVector(2), -1, candidates, null));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));

		recs = extractIds(recommender.recommend(2, getRatingVector(2), 1, candidates, null));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));

		recs = extractIds(recommender.recommend(2, getRatingVector(2), 0, candidates, null));
		assertTrue(recs.isEmpty());

		LongOpenHashSet exclude = new LongOpenHashSet();
		exclude.add(9);
		recs = extractIds(recommender.recommend(2, getRatingVector(2), -1, candidates, exclude));
		assertTrue(recs.isEmpty());

		recs = extractIds(recommender.recommend(5, getRatingVector(5), -1, null, null));
		assertEquals(4, recs.size());
		assertEquals(9, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(6, recs.getLong(2));
		assertEquals(8, recs.getLong(3));

		recs = extractIds(recommender.recommend(5, getRatingVector(5), 5, null, null));
		assertEquals(4, recs.size());
		assertEquals(9, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(6, recs.getLong(2));
		assertEquals(8, recs.getLong(3));

		recs = extractIds(recommender.recommend(5, getRatingVector(5), 4, null, null));
		assertEquals(4, recs.size());
		assertEquals(9, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(6, recs.getLong(2));
		assertEquals(8, recs.getLong(3));

		recs = extractIds(recommender.recommend(5, getRatingVector(5), 3, null, null));
		assertEquals(3, recs.size());
		assertEquals(9, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(6,recs.getLong(2));

		recs = extractIds(recommender.recommend(5, getRatingVector(5), 2, null, null));
		assertEquals(2, recs.size());
		assertEquals(9, recs.getLong(0));
		assertEquals(7, recs.getLong(1));

		recs = extractIds(recommender.recommend(5, getRatingVector(5), 1, null, null));
		assertEquals(1, recs.size());
		assertEquals(9, recs.getLong(0));

		recs = extractIds(recommender.recommend(5, getRatingVector(5), 0, null, null));
		assertTrue(recs.isEmpty());

		candidates.clear();
		candidates.add(6);
		candidates.add(7);
		recs = extractIds(recommender.recommend(6, getRatingVector(6), -1, candidates, null));
		assertEquals(2, recs.size());
		assertTrue(recs.contains(6));
		assertTrue(recs.contains(7));

		candidates.remove(6);
		recs = extractIds(recommender.recommend(6, getRatingVector(6), -1, candidates, null));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(7));

		candidates.remove(7);
		recs = extractIds(recommender.recommend(6, getRatingVector(6), -1, candidates, null));
		assertTrue(recs.isEmpty());

		candidates.add(6);
		candidates.add(7);
		exclude.add(6);
		recs = extractIds(recommender.recommend(6, getRatingVector(6), -1, candidates, exclude));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(7));

		exclude.add(7);
		recs = extractIds(recommender.recommend(6, getRatingVector(6), -1, candidates, exclude));
		assertTrue(recs.isEmpty());

		exclude.remove(6);
		recs = extractIds(recommender.recommend(6, getRatingVector(6), -1, candidates, exclude));
		assertEquals(1, recs.size());
		assertTrue(recs.contains(6));

	}

	//Helper method to retrieve user's ratings and create SparseVector
	private static SparseVector getRatingVector(long user) {
		return Ratings.userRatingVector(dao.getUserRatings(user));
	}

	//Helper method that generates a list of item id's from a list of ScoredId's
	private static LongList extractIds(List<ScoredId> recommendations) {
		LongArrayList ids = new LongArrayList();
		for (ScoredId rec : recommendations) {
			ids.add(rec.getId());
		}
		return ids;
	}

	@AfterClass
	public static void cleanUp() {
		rec.close();
		dao.close();
	}
}
