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

import static org.grouplens.common.test.Matchers.isAssignableTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.ConstantPredictor;
import org.grouplens.lenskit.params.BaselinePredictor;
import org.grouplens.lenskit.params.meta.Parameters;
import org.grouplens.lenskit.svd.params.ClampingFunction;
import org.grouplens.lenskit.svd.params.FeatureCount;
import org.grouplens.lenskit.svd.params.FeatureTrainingThreshold;
import org.grouplens.lenskit.svd.params.GradientDescentRegularization;
import org.grouplens.lenskit.svd.params.IterationCount;
import org.grouplens.lenskit.svd.params.LearningRate;
import org.grouplens.lenskit.testing.RecommenderModuleTest;
import org.grouplens.lenskit.util.DoubleFunction;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Key;
import com.google.inject.Module;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class GradientDescentSVDModuleTest extends RecommenderModuleTest {
    private static final double EPSILON = 1.0e-6;
    private GradientDescentSVDModule module;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        module = new GradientDescentSVDModule();
    }

    protected Module getModule() {
        return module;
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#getFeatureCount()}.
     */
    @Test
    public void testGetFeatureCount() {
        assertEquals(Parameters.getDefaultInt(FeatureCount.class), module.getFeatureCount());
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#setFeatureCount(int)}.
     */
    @Test
    public void testSetFeatureCount() {
        module.setFeatureCount(10);
        assertEquals(10, module.getFeatureCount());
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#getLearningRate()}.
     */
    @Test
    public void testGetLearningRate() {
        assertEquals(Parameters.getDefaultDouble(LearningRate.class), module.getLearningRate(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#setLearningRate(double)}.
     */
    @Test
    public void testSetLearningRate() {
        module.setLearningRate(0.5);
        assertEquals(0.5, module.getLearningRate(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#getFeatureTrainingThreshold()}.
     */
    @Test
    public void testGetFeatureTrainingThreshold() {
        assertEquals(Parameters.getDefaultDouble(FeatureTrainingThreshold.class), module.getFeatureTrainingThreshold(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#setFeatureTrainingThreshold(double)}.
     */
    @Test
    public void testSetFeatureTrainingThreshold() {
        module.setFeatureTrainingThreshold(1);
        assertEquals(1, module.getFeatureTrainingThreshold(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#getGradientDescentRegularization()}.
     */
    @Test
    public void testGetGradientDescentRegularization() {
        assertEquals(Parameters.getDefaultDouble(GradientDescentRegularization.class), module.getGradientDescentRegularization(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#setGradientDescentRegularization(double)}.
     */
    @Test
    public void testSetGradientDescentRegularization() {
        module.setGradientDescentRegularization(1);
        assertEquals(1, module.getGradientDescentRegularization(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#getIterationCount()}.
     */
    @Test
    public void testGetIterationCount() {
        assertEquals(Parameters.getDefaultInt(IterationCount.class), module.getIterationCount());
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#setIterationCount(double)}.
     */
    @Test
    public void testSetIterationCount() {
        module.setIterationCount(120);
        assertEquals(120, module.getIterationCount());
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#getClampingFunction()}.
     */
    @Test
    public void testGetClampingFunction() {
        assertThat(module.getClampingFunction(), isAssignableTo(DoubleFunction.Identity.class));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.svd.GradientDescentSVDModule#setClampingFunction(java.lang.Class)}.
     */
    @Test
    public void testSetClampingFunction() {
        module.setClampingFunction(RatingRangeClamp.class);
        DoubleFunction clamp = inject(Key.get(DoubleFunction.class, ClampingFunction.class));
        assertThat(clamp, instanceOf(RatingRangeClamp.class));
    }

    @Test
    public void testDefaultInject() {
        module.core.setBaseline(ConstantPredictor.class);
        module.core.setConstantBaselineValue(3);
        GradientDescentSVDRecommenderBuilder builder =
            inject(Key.get(GradientDescentSVDRecommenderBuilder.class));
        assertThat(builder, instanceOf(GradientDescentSVDRecommenderBuilder.class));
        GradientDescentSVDRecommenderBuilder b = (GradientDescentSVDRecommenderBuilder) builder;
        assertEquals(Parameters.getDefaultInt(FeatureCount.class), b.featureCount);
        assertEquals(Parameters.getDefaultDouble(LearningRate.class), b.learningRate, EPSILON);
        assertEquals(Parameters.getDefaultDouble(GradientDescentRegularization.class), b.trainingRegularization, EPSILON);
        assertEquals(Parameters.getDefaultDouble(FeatureTrainingThreshold.class), b.trainingThreshold, EPSILON);
        assertEquals(Parameters.getDefaultInt(IterationCount.class), b.iterationCount);
        assertThat(b.clampingFunction, instanceOf(DoubleFunction.Identity.class));
        RatingPredictor baseline = inject(Key.get(RatingPredictor.class, BaselinePredictor.class));
        assertNotNull(baseline);
        assertThat(baseline, instanceOf(ConstantPredictor.class));
    }
}
