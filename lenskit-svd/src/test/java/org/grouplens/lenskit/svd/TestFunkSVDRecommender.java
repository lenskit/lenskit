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
package org.grouplens.lenskit.svd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.UserMeanPredictor;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.grouplens.lenskit.svd.params.FeatureCount;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Unstable based on parameters")
public class TestFunkSVDRecommender {
	
	private static Recommender svdRecommender;
	private static ItemRecommender recommender;
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
		factory.setComponent(RatingPredictor.class, FunkSVDRatingPredictor.class);
		factory.setComponent(BaselinePredictor.class, UserMeanPredictor.class);
		factory.setComponent(ItemRecommender.class, FunkSVDRecommender.class);
		// FIXME: Don't use 100 features.
		factory.set(FeatureCount.class, 100);
		RecommenderEngine engine = factory.create();
		svdRecommender = engine.open();
		recommender = svdRecommender.getItemRecommender();
		dao = manager.create();
	}
	
	
	/**
	 * Tests <tt>recommend(long)</tt>.
	 */
	@Test
	public void testRecommend1() {
		
		LongList recs = recommender.recommend(1);
		assertTrue(recs.isEmpty());
		
		recs = recommender.recommend(2);
		assertEquals(1, recs.size());
		assertTrue(recs.contains(9));
		
		recs = recommender.recommend(3);
		assertEquals(1, recs.size());
		assertTrue(recs.contains(6));
		
		recs = recommender.recommend(4);
		assertEquals(2, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(9, recs.getLong(1));
		
		recs = recommender.recommend(5);
		assertEquals(3, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(9, recs.getLong(2));
		
		recs = recommender.recommend(6);
		assertEquals(3, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(9, recs.getLong(2));
	}
	
	/**
	 * Tests <tt>recommend(long, int)</tt>.
	 */
	@Test
	public void testRecommend2() {
		
		LongList recs = recommender.recommend(6, 4);
		assertEquals(3, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(9, recs.getLong(2));
		
		recs = recommender.recommend(6, 3);
		assertEquals(3, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(9, recs.getLong(2));
		
		recs = recommender.recommend(6, 2);
		assertEquals(2, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		
		recs = recommender.recommend(6, 1);
		assertEquals(1, recs.size());
		assertTrue(recs.contains(6));
		
		recs = recommender.recommend(6, 0);
		assertTrue(recs.isEmpty());
		
		recs = recommender.recommend(6, -1);
		assertEquals(3, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(9, recs.getLong(2));
	}
	
	/**
	 * Tests <tt>recommend(long, Set)</tt>.
	 */
	@Test
	public void testRecommend3() {
		
		LongList recs = recommender.recommend(5, null);
		assertEquals(3, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(9, recs.getLong(2));
		
		LongOpenHashSet candidates = new LongOpenHashSet();
		candidates.add(6);
		candidates.add(7);
		candidates.add(8);
		candidates.add(9);
		recs = recommender.recommend(5, candidates);
		assertEquals(3, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(9, recs.getLong(2));
		
		candidates.remove(8);
		recs = recommender.recommend(5, candidates);
		assertEquals(3, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(9, recs.getLong(2));
		
		candidates.remove(7);
		recs = recommender.recommend(5, candidates);
		assertEquals(2, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(9, recs.getLong(1));
		
		candidates.remove(6);
		recs = recommender.recommend(5, candidates);
		assertEquals(1, recs.size());
		assertEquals(9, recs.getLong(0));
		
		candidates.remove(9);
		recs = recommender.recommend(5, candidates);
		assertTrue(recs.isEmpty());
		
		candidates.add(8);
		recs = recommender.recommend(5, candidates);
		assertTrue(recs.isEmpty());
	}
	
	/**
	 * Tests <tt>recommend(long, int, Set, Set)</tt>.
	 */
	@Test
	public void testRecommend4() {
		LongList recs = recommender.recommend(6, -1, null, null);
		assertEquals(4, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(9, recs.getLong(2));
		assertEquals(8, recs.getLong(3));
		
		LongOpenHashSet exclude = new LongOpenHashSet();
		exclude.add(9);
		recs = recommender.recommend(6, -1, null, exclude);
		assertEquals(3, recs.size());
		assertEquals(6, recs.getLong(0));
		assertEquals(7, recs.getLong(1));
		assertEquals(8, recs.getLong(2));
		
		exclude.add(6);
		recs = recommender.recommend(6, -1, null, exclude);
		assertEquals(2, recs.size());
		assertEquals(7, recs.getLong(0));
		assertEquals(8, recs.getLong(1));
		
		exclude.add(8);
		recs = recommender.recommend(6, -1, null, exclude);
		assertEquals(1, recs.size());
		assertTrue(recs.contains(7));
	}
	
	@AfterClass
	public static void cleanUp() {
		svdRecommender.close();
		dao.close();
	}
}