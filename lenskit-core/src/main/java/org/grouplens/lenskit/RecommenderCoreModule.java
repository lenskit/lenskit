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
package org.grouplens.lenskit;

import javax.annotation.Nullable;

import org.grouplens.lenskit.baseline.ConstantPredictor;
import org.grouplens.lenskit.params.BaselinePredictor;
import org.grouplens.lenskit.params.MaxRating;
import org.grouplens.lenskit.params.MeanDamping;
import org.grouplens.lenskit.params.MinRating;
import org.grouplens.lenskit.params.RecommenderName;
import org.grouplens.lenskit.params.ThreadCount;
import org.grouplens.lenskit.util.TypeUtils;

import com.google.inject.Provides;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.util.Providers;

/**
 * Module for configuring the common LensKit parameters.
 *
 * <p>This module provides bean properties and bindings for the core LensKit
 * parameters, controlling the infrastructure and the common parameters used
 * across many algorithms.
 *
 * @todo Document this module.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class RecommenderCoreModule extends RecommenderModuleComponent {
    private @ThreadCount int threadCount = Runtime.getRuntime().availableProcessors();
    private @MeanDamping double meanDamping;
    private @MinRating double minRating;
    private @MaxRating double maxRating;
    private @BaselinePredictor @Nullable Class<? extends RatingPredictor> baseline;
    private @ConstantPredictor.Value double constantBaselineValue;

    public RecommenderCoreModule() {
        initializeDefaultValues(this.getClass());
        baseline = null;
    }

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        getLogger().debug("Configuring core recommender module");
        configureBaseline();
    }

    /**
     * Provide the recommender name to the module.
     */
    @Provides @RecommenderName
    public String getName() {
        return super.getName();
    }

    @Provides @ThreadCount
    public int getThreadCount() {
        return threadCount;
    }
    public void setThreadCount(int count) {
        threadCount = count;
    }

    /**
     * @return the meanDamping
     */
    @Provides @MeanDamping
    public double getMeanDamping() {
        return meanDamping;
    }

    /**
     * @param meanDamping the meanDamping to set
     */
    public void setMeanDamping(double damping) {
        this.meanDamping = damping;
    }

    /**
     * @return the minRating
     */
    @Provides @MinRating
    public double getMinRating() {
        return minRating;
    }

    /**
     * @param minRating the minRating to set
     */
    public void setMinRating(double minRating) {
        this.minRating = minRating;
    }

    /**
     * @return the maxRating
     */
    @Provides @MaxRating
    public double getMaxRating() {
        return maxRating;
    }

    /**
     * @param maxRating the maxRating to set
     */
    public void setMaxRating(double maxRating) {
        this.maxRating = maxRating;
    }

    /**
     * Configure the binding for the baseline predictor.
     *
     * @todo Make this capable of reifying generic types with
     * {@link TypeUtils#reifyType(Type, Class)}.
     */
    protected void configureBaseline() {
        LinkedBindingBuilder<RatingPredictor> binder = bind(RatingPredictor.class).annotatedWith(BaselinePredictor.class);
        if (baseline == null)
            binder.toProvider(Providers.of((RatingPredictor) null));
        else
            binder.to(baseline);
    }

    public Class<? extends RatingPredictor> getBaseline() {
        return baseline;
    }

    /**
     * Set the predictor used for the baseline.
     * @todo Support setting baseline providers.
     * @param cls The class.
     */
    public void setBaseline(Class<? extends RatingPredictor> cls) {
        baseline = cls;
    }

    @Provides @ConstantPredictor.Value
    public double getConstantBaselineValue() {
        return constantBaselineValue;
    }

    public void setConstantBaselineValue(double v) {
        constantBaselineValue = v;
    }
}
