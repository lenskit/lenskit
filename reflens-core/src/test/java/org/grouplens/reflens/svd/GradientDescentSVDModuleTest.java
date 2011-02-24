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
package org.grouplens.reflens.svd;

import static org.grouplens.common.test.GuiceHelpers.inject;
import static org.grouplens.common.test.Matchers.isAssignableTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.baseline.ConstantPredictor;
import org.grouplens.reflens.params.BaselinePredictor;
import org.grouplens.reflens.params.meta.Parameters;
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
		assertEquals(Parameters.getDefaultInt(FeatureCount.class), module.getFeatureCount());
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
		assertEquals(Parameters.getDefaultDouble(LearningRate.class), module.getLearningRate(), EPSILON);
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
		assertEquals(Parameters.getDefaultDouble(FeatureTrainingThreshold.class), module.getFeatureTrainingThreshold(), EPSILON);
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
		assertEquals(Parameters.getDefaultDouble(GradientDescentRegularization.class), module.getGradientDescentRegularization(), EPSILON);
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
		assertEquals(Parameters.getDefaultInt(IterationCount.class), module.getIterationCount());
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
		RecommenderBuilder builder = inject(module, RecommenderBuilder.class);
		assertThat(builder, instanceOf(GradientDescentSVDRecommenderBuilder.class));
		GradientDescentSVDRecommenderBuilder b = (GradientDescentSVDRecommenderBuilder) builder;
		assertEquals(Parameters.getDefaultInt(FeatureCount.class), b.featureCount);
		assertEquals(Parameters.getDefaultDouble(LearningRate.class), b.learningRate, EPSILON);
		assertEquals(Parameters.getDefaultDouble(GradientDescentRegularization.class), b.trainingRegularization, EPSILON);
		assertEquals(Parameters.getDefaultDouble(FeatureTrainingThreshold.class), b.trainingThreshold, EPSILON);
		assertEquals(Parameters.getDefaultInt(IterationCount.class), b.iterationCount);
		assertThat(b.clampingFunction, instanceOf(DoubleFunction.Identity.class));
		RatingPredictor baseline = inject(module, RatingPredictor.class, BaselinePredictor.class);
		assertNotNull(baseline);
		assertThat(baseline, instanceOf(ConstantPredictor.class));
	}
}
