/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.lenskit.iterative;

import com.google.common.base.Preconditions;
import org.lenskit.inject.Shareable;

import net.jcip.annotations.Immutable;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * Stop when absolute value of the error drops below a threshold.
 * <p><b>Note:</b> Loop controllers created by
 * this stopping condition support waiting until the threshold has been met for multiple iterations
 * to stop.
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
