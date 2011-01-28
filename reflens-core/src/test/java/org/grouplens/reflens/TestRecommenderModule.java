package org.grouplens.reflens;

import static org.grouplens.common.test.Matchers.isAssignableTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import static org.grouplens.common.test.GuiceHelpers.*;

import org.grouplens.reflens.baseline.ConstantPredictor;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.params.BaselinePredictor;
import org.grouplens.reflens.params.MaxRating;
import org.grouplens.reflens.params.MeanDamping;
import org.grouplens.reflens.params.MinRating;
import org.grouplens.reflens.params.ThreadCount;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestRecommenderModule {
	public static final double EPSILON = 1.0e-6;
	private RecommenderModule module;
	
	@Before
	public void setUp() {
		module = new RecommenderModule();
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setName(java.lang.String)}.
	 */
	@Test
	public void testSetName() {
		module.setName("foo");
		assertEquals("foo", module.getName());
	}
	
	@Test
	public void testDefaults() {
		assertEquals(MeanDamping.DEFAULT_VALUE, module.getDamping(), EPSILON);
		assertEquals(MinRating.DEFAULT_VALUE, module.getMinRating(), EPSILON);
		assertEquals(MaxRating.DEFAULT_VALUE, module.getMaxRating(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#getThreadCount()}.
	 */
	@Test
	public void testGetThreadCount() {
		assertEquals(Runtime.getRuntime().availableProcessors(), module.getThreadCount());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setThreadCount(int)}.
	 */
	@Test
	public void testSetThreadCount() {
		// 57 is a safe processor count to make sure the set method changes it
		// unless you are on a very strange machine
		module.setThreadCount(57);
		assertEquals(57, module.getThreadCount());
	}
	
	@Test
	public void testInjectThreadCount() {
		int nthreads = inject(module, int.class, ThreadCount.class);
		assertEquals(Runtime.getRuntime().availableProcessors(), nthreads);
		module.setThreadCount(57);
		nthreads = inject(module, int.class, ThreadCount.class);
		assertEquals(57, nthreads);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setDamping(double)}.
	 */
	@Test
	public void testSetDamping() {
		module.setDamping(500);
		assertEquals(500, module.getDamping(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setMinRating(double)}.
	 */
	@Test
	public void testSetMinRating() {
		module.setMinRating(-5);
		assertEquals(-5, module.getMinRating(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setMaxRating(double)}.
	 */
	@Test
	public void testSetMaxRating() {
		module.setMaxRating(42);
		assertEquals(42, module.getMaxRating(), EPSILON);
	}
	
	public void testInjectRatingRange() {
		double min = inject(module, double.class, MinRating.class);
		double max = inject(module, double.class, MaxRating.class);
		assertEquals(1, min, EPSILON);
		assertEquals(5, max, EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#getBaseline()}.
	 */
	@Test
	public void testGetBaseline() {
		assertNull(module.getBaseline());
		assertNull(inject(module, RatingPredictorBuilder.class, BaselinePredictor.class));
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setBaseline(java.lang.Class)}.
	 */
	@Test
	public void testSetBaselineClass() {
		module.setBaseline(DudBaseline.class);
		assertThat(module.getBaseline(), isAssignableTo(DudBaseline.class));
		RatingPredictorBuilder bldr =
			inject(module, RatingPredictorBuilder.class, BaselinePredictor.class);
		assertThat(bldr, instanceOf(DudBaseline.class));
	}
	
	private static class DudBaseline implements RatingPredictorBuilder {
		@Override public RatingPredictor build(RatingDataSource data) {
			throw new UnsupportedOperationException();
		}
	}
	
	@Test
	public void testGetConstantBaselineValue() {
		assertEquals(ConstantPredictor.Value.DEFAULT_VALUE, module.getConstantBaselineValue(), EPSILON);
	}
	
	@Test
	public void testSetConstantBaselineValue() {
		module.setConstantBaselineValue(10);
		assertEquals(10, module.getConstantBaselineValue(), EPSILON);
	}
	
	@Test
	public void testInjectConstantBaseline() {
		module.setConstantBaselineValue(3);
		module.setBaseline(ConstantPredictor.class);
		RatingPredictorBuilder pred = inject(module, RatingPredictorBuilder.class, BaselinePredictor.class);
		assertThat(pred, instanceOf(ConstantPredictor.Builder.class));
		ConstantPredictor.Builder b = (ConstantPredictor.Builder) pred;
		assertEquals(3, b.getValue(), EPSILON);
	}
}
