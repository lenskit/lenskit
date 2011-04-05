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

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderModule;
import org.grouplens.lenskit.svd.params.ClampingFunction;
import org.grouplens.lenskit.svd.params.FeatureCount;
import org.grouplens.lenskit.svd.params.FeatureTrainingThreshold;
import org.grouplens.lenskit.svd.params.GradientDescentRegularization;
import org.grouplens.lenskit.svd.params.IterationCount;
import org.grouplens.lenskit.svd.params.LearningRate;
import org.grouplens.lenskit.util.DoubleFunction;

import com.google.inject.Provides;

public class GradientDescentSVDModule extends RecommenderModule {
    private int featureCount = 100;
    private double learningRate = 0.001;
    private double featureTrainingThreshold = 1.0e-5;
    private double gradientDescentRegularization = 0.015;
    private int iterationCount = 0;
    private Class<? extends DoubleFunction> clampingFunction = DoubleFunction.Identity.class;

    public GradientDescentSVDModule() {
        super();
    }

    @Override
    protected void configure() {
        super.configure();
        configureClamping();
        bind(SVDModelBuilder.class).to(GradientDescentSVDModelBuilder.class);
        bind(RatingPredictor.class).to(SVDRatingPredictor.class);
    }

    protected void configureClamping() {
        bind(DoubleFunction.class).annotatedWith(ClampingFunction.class).to(clampingFunction);
    }

    /**
     * @return the featureCount
     */
    @Provides @FeatureCount
    public int getFeatureCount() {
        return featureCount;
    }

    /**
     * @param featureCount the featureCount to set
     */
    public void setFeatureCount(int featureCount) {
        this.featureCount = featureCount;
    }

    /**
     * @return the learningRate
     */
    @Provides @LearningRate
    public double getLearningRate() {
        return learningRate;
    }

    /**
     * @param learningRate the learningRate to set
     */
    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    /**
     * @return the featureTrainingThreshold
     */
    @Provides @FeatureTrainingThreshold
    public double getFeatureTrainingThreshold() {
        return featureTrainingThreshold;
    }

    /**
     * @param featureTrainingThreshold the featureTrainingThreshold to set
     */
    public void setFeatureTrainingThreshold(double featureTrainingThreshold) {
        this.featureTrainingThreshold = featureTrainingThreshold;
    }

    /**
     * @return the gradientDescentRegularization
     */
    @Provides @GradientDescentRegularization
    public double getGradientDescentRegularization() {
        return gradientDescentRegularization;
    }

    /**
     * @param gradientDescentRegularization the gradientDescentRegularization to set
     */
    public void setGradientDescentRegularization(
            double gradientDescentRegularization) {
        this.gradientDescentRegularization = gradientDescentRegularization;
    }

    /**
     * @return the iterationCount
     */
    @Provides @IterationCount
    public int getIterationCount() {
        return iterationCount;
    }

    /**
     * @param iterationCount the iterationCount to set
     */
    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    /**
     * @return the clampingFunction
     */
    public Class<? extends DoubleFunction> getClampingFunction() {
        return clampingFunction;
    }

    /**
     * @param clampingFunction the clampingFunction to set
     */
    public void setClampingFunction(Class<? extends DoubleFunction> clampingFunction) {
        this.clampingFunction = clampingFunction;
    }
}