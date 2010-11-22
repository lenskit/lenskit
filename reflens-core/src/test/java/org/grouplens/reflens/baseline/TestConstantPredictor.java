/**
 * 
 */
package org.grouplens.reflens.baseline;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingCollectionDataSource;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.ScoredId;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestConstantPredictor {
	
	private RatingPredictorBuilder builder;

	@Before
	public void setUp() {
		builder = new ConstantPredictor.Builder(5);
	}
	
	@Test
	public void testNullBuild() {
		RatingPredictor pred = builder.build(null);
		Map<Long,Double> map = Collections.emptyMap();
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(5, score.getScore(), 0.00001);
	}
	
	@Test
	public void testDataSourceBuild() {
		List<Rating> ratings = Collections.emptyList();
		Map<Long,Double> map = Collections.emptyMap();
		RatingDataSource source = new RatingCollectionDataSource(ratings);
		RatingPredictor pred = builder.build(source);
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(5, score.getScore(), 0.00001);
	}
}
