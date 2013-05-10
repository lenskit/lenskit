/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.iterative;

import com.google.common.base.Preconditions;
import org.grouplens.lenskit.core.Shareable;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * Stop when absolute value of the error drops below a threshold.
 * <p><b>Note:</b> Loop controllers created by
 * this stopping condition support waiting until the threshold has been met for multiple iterations
 * to stop.  This behavior is not supported by the deprecated {@link #isFinished(int, double)}
 * method.
 * <p>This stopping condition differs from {@link ThresholdStoppingCondition}
 * in that it thresholds the absolute error instead of the change in error from
 * one iteration to another.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
@Immutable
@Shareable
public class ErrorThresholdStoppingCondition implements StoppingCondition, Serializable {
    private static final long serialVersionUID = 2L;

    private final double threshold;
    private final int minIterations;

    /**
     * Construct a new error threshold stop.
     *
     * @param thresh      The threshold.
     * @param minIter     The minimum number of iterations.
     */
    @Inject
    public ErrorThresholdStoppingCondition(@StoppingThreshold double thresh,
                                           @MinimumIterations int minIter) {
        Preconditions.checkArgument(thresh > 0, "threshold must be positive");
        threshold = thresh;
        minIterations = minIter;
    }

    /**
     * Create a new threshold stop with no minimum iteration count.
     *
     * @param thresh The threshold value.
     */
    public ErrorThresholdStoppingCondition(double thresh) {
        this(thresh, 0);
    }

    @Override
    @Deprecated
    public boolean isFinished(int n, double d) {
        return n >= minIterations && Math.abs(d) < threshold;
    }

    @Override
    public TrainingLoopController newLoop() {
        return new Controller();
    }

    /**
     * Get the stopper's threshold.
     *
     * @return The stopper's threshold.
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Get the minimum iteration count.
     *
     * @return The number of iterations to require.
     */
    public int getMinimumIterations() {
        return minIterations;
    }

    @Override
    public String toString() {
        return String.format("Stop(threshold=%f, minIters=%d)", threshold, minIterations);
    }

    /**
     * Threshold Training Loop controller for iterative updates
     */
    private class Controller implements TrainingLoopController {
        private int iterations = 0;
        private int goodIters = 0;

        @Override
        public boolean keepTraining(double error) {
            if (Math.abs(error) < threshold) {
                goodIters += 1;
            }
            if (iterations >= minIterations) {
                return false;
            } else {
                ++iterations;
                return true;
            }
        }

        @Override
        public int getIterationCount() {
            return iterations;
        }
    }
}
