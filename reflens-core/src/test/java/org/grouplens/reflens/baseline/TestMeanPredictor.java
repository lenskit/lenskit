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
