/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

/**
 * 
 */
package org.grouplens.reflens.baseline;


import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingCollectionDataSource;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.ScoredId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test baseline predictors that compute means from data.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestMeanPredictor {
	private static final double RATINGS_DAT_MEAN = 3.75;
	private RatingDataSource ratings;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new Rating(1, 5, 2));
		rs.add(new Rating(1, 7, 4));
		rs.add(new Rating(8, 4, 5));
		rs.add(new Rating(8, 5, 4));
		ratings = new RatingCollectionDataSource(rs);
	}
	
	@Test
	public void testMeanBaseline() {
		RatingPredictorBuilder builder = new GlobalMeanPredictor.Builder();
		RatingPredictor pred = builder.build(ratings);
		RatingVector map = new RatingVector();
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(RATINGS_DAT_MEAN, score.getScore(), 0.00001);
	}
	
	@Test
	public void testUserMeanBaseline() {
		RatingPredictorBuilder builder = new UserMeanPredictor.Builder();
		RatingPredictor pred = builder.build(ratings);
		RatingVector map = new RatingVector();
		map.put(5, 3);
		map.put(7, 6);
		map.put(10, 4);
		// unseen item
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(4.33333, score.getScore(), 0.001);
		// seen item - should be same avg
		score = pred.predict(10l, map, 7);
		assertEquals(4.33333, score.getScore(), 0.001);
	}
	
	@Test
	public void testUserMeanBaselineNoFastutil() {
		RatingPredictorBuilder builder = new UserMeanPredictor.Builder();
		RatingPredictor pred = builder.build(ratings);
		RatingVector map = new RatingVector();
		map.put(5l, 3.0);
		map.put(7l, 6.0);
		map.put(10l, 4.0);
		// unseen item
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(4.33333, score.getScore(), 0.001);
		// seen item - should be same avg
		score = pred.predict(10l, map, 7);
		assertEquals(4.33333, score.getScore(), 0.001);
		
		// try twice
		LongCollection items = new LongArrayList();
		items.add(7);
		items.add(2);
		RatingVector preds = pred.predict(10l, map, items);
		assertEquals(4.33333, preds.get(2l), 0.001);
		assertEquals(4.33333, preds.get(7l), 0.001);
	}
	
	/**
	 * Test falling back to an empty user.
	 */
	@Test
	public void testUserMeanBaselineFallback() {
		RatingPredictorBuilder builder = new UserMeanPredictor.Builder();
		RatingPredictor pred = builder.build(ratings);
		RatingVector map = new RatingVector();
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(RATINGS_DAT_MEAN, score.getScore(), 0.001);
	}
	
	@Test
	public void testItemMeanBaseline() {
		RatingPredictorBuilder builder = new ItemMeanPredictor.Builder();
		RatingPredictor pred = builder.build(ratings);
		RatingVector map = new RatingVector();
		map.put(5, 3);
		map.put(7, 6);
		map.put(10, 4);
		// unseen item, should be global mean
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(RATINGS_DAT_MEAN, score.getScore(), 0.001);
		// seen item - should be item average
		score = pred.predict(10l, map, 5);
		assertEquals(3.0, score.getScore(), 0.001);
		
		// try twice
		LongCollection items = new LongArrayList();
		items.add(5);
		items.add(2);
		RatingVector preds = pred.predict(10l, map, items);
		assertEquals(RATINGS_DAT_MEAN, preds.get(2l), 0.001);
		assertEquals(3.0, preds.get(5l), 0.001);
	}
	
	@Test
	public void testUserItemMeanBaseline() {
		RatingPredictorBuilder builder = new ItemUserMeanPredictor.Builder();
		RatingPredictor pred = builder.build(ratings);
		RatingVector map = new RatingVector();
		map.put(5, 3); // offset = 0
		map.put(7, 6); // offset = 2
		map.put(10, 4); // offset = 4 - µ = 0.25
		final double avgOffset = 0.75;
		
		// unseen item, should be global mean + user offset
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(RATINGS_DAT_MEAN + avgOffset, score.getScore(), 0.001);
		// seen item - should be item average + user offset
		score = pred.predict(10l, map, 5);
		assertEquals(3.0 + avgOffset, score.getScore(), 0.001);
		
		// try twice
		LongCollection items = new LongArrayList();
		items.add(5);
		items.add(2);
		RatingVector preds = pred.predict(10l, map, items);
		assertEquals(RATINGS_DAT_MEAN + avgOffset, preds.get(2l), 0.001);
		assertEquals(3.0 + avgOffset, preds.get(5l), 0.001);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		ratings.close();
	}

}
