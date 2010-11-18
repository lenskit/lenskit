/**
 * 
 */
package org.grouplens.reflens.baseline;


import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.data.SimpleFileDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestMeanBaselinePredictor {
	private RatingDataSource ratings;
	private RatingPredictorBuilder builder;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		ratings = new SimpleFileDataSource(TestMeanBaselinePredictor.class.getResource("ratings.dat"), " ");
		builder = new MeanBaselinePredictor.Builder();
	}
	
	@Test
	public void testMeanRating() {
		RatingPredictor pred = builder.build(ratings);
		Map<Long,Double> map = Collections.emptyMap();
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(3.75, score.getScore(), 0.00001);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		ratings.close();
	}

}
