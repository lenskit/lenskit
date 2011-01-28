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
