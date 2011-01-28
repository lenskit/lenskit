package org.grouplens.reflens.svd;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.grouplens.common.test.Matchers.*;
import static org.grouplens.common.test.GuiceHelpers.*;

import org.grouplens.reflens.RecommenderEngineBuilder;
import org.grouplens.reflens.baseline.ConstantPredictor;
import org.grouplens.reflens.svd.params.ClampingFunction;
import org.grouplens.reflens.svd.params.FeatureCount;
import org.grouplens.reflens.svd.params.FeatureTrainingThreshold;
import org.grouplens.reflens.svd.params.GradientDescentRegularization;
import org.grouplens.reflens.svd.params.IterationCount;
import org.grouplens.reflens.svd.params.LearningRate;
import org.grouplens.reflens.util.DoubleFunction;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class GradientDescentSVDModuleTest {
	private static final double EPSILON = 1.0e-6;
	private GradientDescentSVDModule module;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		module = new GradientDescentSVDModule();
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#getFeatureCount()}.
	 */
	@Test
	public void testGetFeatureCount() {
		assertEquals(FeatureCount.DEFAULT_VALUE, module.getFeatureCount());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#setFeatureCount(int)}.
	 */
	@Test
	public void testSetFeatureCount() {
		module.setFeatureCount(10);
		assertEquals(10, module.getFeatureCount());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#getLearningRate()}.
	 */
	@Test
	public void testGetLearningRate() {
		assertEquals(LearningRate.DEFAULT_VALUE, module.getLearningRate(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#setLearningRate(double)}.
	 */
	@Test
	public void testSetLearningRate() {
		module.setLearningRate(0.5);
		assertEquals(0.5, module.getLearningRate(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#getFeatureTrainingThreshold()}.
	 */
	@Test
	public void testGetFeatureTrainingThreshold() {
		assertEquals(FeatureTrainingThreshold.DEFAULT_VALUE, module.getFeatureTrainingThreshold(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#setFeatureTrainingThreshold(double)}.
	 */
	@Test
	public void testSetFeatureTrainingThreshold() {
		module.setFeatureTrainingThreshold(1);
		assertEquals(1, module.getFeatureTrainingThreshold(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#getGradientDescentRegularization()}.
	 */
	@Test
	public void testGetGradientDescentRegularization() {
		assertEquals(GradientDescentRegularization.DEFAULT_VALUE, module.getGradientDescentRegularization(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#setGradientDescentRegularization(double)}.
	 */
	@Test
	public void testSetGradientDescentRegularization() {
		module.setGradientDescentRegularization(1);
		assertEquals(1, module.getGradientDescentRegularization(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#getIterationCount()}.
	 */
	@Test
	public void testGetIterationCount() {
		assertEquals(IterationCount.DEFAULT_VALUE, module.getIterationCount());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#setIterationCount(double)}.
	 */
	@Test
	public void testSetIterationCount() {
		module.setIterationCount(120);
		assertEquals(120, module.getIterationCount());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#getClampingFunction()}.
	 */
	@Test
	public void testGetClampingFunction() {
		assertThat(module.getClampingFunction(), isAssignableTo(DoubleFunction.Identity.class));
	}

	/**
	 * Test method for {@link org.grouplens.reflens.svd.GradientDescentSVDModule#setClampingFunction(java.lang.Class)}.
	 */
	@Test
	public void testSetClampingFunction() {
		module.setClampingFunction(RatingRangeClamp.class);
		DoubleFunction clamp = inject(module, DoubleFunction.class, ClampingFunction.class);
		assertThat(clamp, instanceOf(RatingRangeClamp.class));
	}
	
	@Test
	public void testDefaultInject() {
		module.setBaseline(ConstantPredictor.class);
		module.setConstantBaselineValue(3);
		RecommenderEngineBuilder builder = inject(module, RecommenderEngineBuilder.class);
		assertThat(builder, instanceOf(GradientDescentSVDRecommenderBuilder.class));
		GradientDescentSVDRecommenderBuilder b = (GradientDescentSVDRecommenderBuilder) builder;
		assertEquals(FeatureCount.DEFAULT_VALUE, b.featureCount);
		assertEquals(LearningRate.DEFAULT_VALUE, b.learningRate, EPSILON);
		assertEquals(GradientDescentRegularization.DEFAULT_VALUE, b.trainingRegularization, EPSILON);
		assertEquals(FeatureTrainingThreshold.DEFAULT_VALUE, b.trainingThreshold, EPSILON);
		assertEquals(IterationCount.DEFAULT_VALUE, b.iterationCount);
		assertThat(b.clampingFunction, instanceOf(DoubleFunction.Identity.class));
		assertNotNull(b.baselineBuilder);
		assertThat(b.baselineBuilder, instanceOf(ConstantPredictor.Builder.class));
		assertEquals(3, ((ConstantPredictor.Builder) b.baselineBuilder).getValue(), EPSILON);
	}
}
