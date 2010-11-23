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
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.data.SimpleFileDataSource;
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
		URL res = this.getClass().getResource("ratings.dat");
		if (res == null) {
			throw new FileNotFoundException("cannot find ratings.dat");
		}
		ratings = new SimpleFileDataSource(res, " ");
	}
	
	@Test
	public void testMeanBaseline() {
		RatingPredictorBuilder builder = new GlobalMeanPredictor.Builder();
		RatingPredictor pred = builder.build(ratings);
		Map<Long,Double> map = Collections.emptyMap();
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(RATINGS_DAT_MEAN, score.getScore(), 0.00001);
	}
	
	@Test
	public void testUserMeanBaseline() {
		RatingPredictorBuilder builder = new UserMeanPredictor.Builder();
		RatingPredictor pred = builder.build(ratings);
		Long2DoubleMap map = new Long2DoubleOpenHashMap();
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
		Map<Long,Double> map = new TreeMap<Long, Double>();
		map.put(5l, 3.0);
		map.put(7l, 6.0);
		map.put(10l, 4.0);
		// unseen item
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(4.33333, score.getScore(), 0.001);
		// seen item - should be same avg
		score = pred.predict(10l, map, 7);
		assertEquals(4.33333, score.getScore(), 0.001);
	}
	
	/**
	 * Test falling back to an empty user.
	 */
	@Test
	public void testUserMeanBaselineFallback() {
		RatingPredictorBuilder builder = new UserMeanPredictor.Builder();
		RatingPredictor pred = builder.build(ratings);
		Map<Long,Double> map = Collections.emptyMap();
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(RATINGS_DAT_MEAN, score.getScore(), 0.001);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		ratings.close();
	}

}
