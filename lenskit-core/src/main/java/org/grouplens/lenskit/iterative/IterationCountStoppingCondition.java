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

import org.lenskit.inject.Shareable;

import net.jcip.annotations.Immutable;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * Stop once the iteration count has reached a value.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Immutable
@Shareable
public class IterationCountStoppingCondition implements StoppingCondition, Serializable {
    private static final long serialVersionUID = 1L;

    private final int iterCount;

    /**
     * Construct a new iteration count stopping condition.
     *
     * @param niter The number of iterations to run.
     */
    @Inject
    public IterationCountStoppingCondition(@IterationCount int niter) {
        iterCount = niter;
    }

    /**
     * Get the number of iterations the stopper requires.
     *
     * @return The number of iterations.
     */
    public int getIterationCount() {
        return iterCount;
    }

    @Override
    public TrainingLoopController newLoop() {
        return new Controller();
    }

    @Override
    public String toString() {
        return String.format("Stop(iterations=%d)", iterCount);
    }

    /**
     * Iteration Count Training Loop controller for iterative updates.
     */
    private class Controller implements TrainingLoopController {
        private int iterations = 0;

        @Override
        public boolean keepTraining(double error) {
            if (iterations >= iterCount) {
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
