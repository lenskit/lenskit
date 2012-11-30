/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.iterative.params.MinimumIterations;
import org.grouplens.lenskit.iterative.params.StoppingThreshold;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * Stop when absolute delta drops below a threshold.
 *
 * @author Michael Ekstrand
 * @since 0.11
 */
@Immutable
@Shareable
public class ThresholdStoppingCondition implements StoppingCondition, Serializable {
    private static final long serialVersionUID = 1L;

    private final double threshold;
    private final int minIterations;

    /**
     * Construct a new threshold stop.
     *
     * @param thresh  The threshold.
     * @param minIter The minimum number of iterations.
     */
    @Inject
    public ThresholdStoppingCondition(@StoppingThreshold double thresh,
                                      @MinimumIterations int minIter) {
        threshold = thresh;
        minIterations = minIter;
    }

    /**
     * Create a new threshold stop with no minimum iteration count.
     *
     * @param thresh The threshold value.
     */
    public ThresholdStoppingCondition(double thresh) {
        this(thresh, 0);
    }

    @Override
    public boolean isFinished(int n, double d) {
        return n >= minIterations && Math.abs(d) < threshold;
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
}
