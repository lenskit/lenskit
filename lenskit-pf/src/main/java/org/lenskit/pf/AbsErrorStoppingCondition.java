/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.pf;

import net.jcip.annotations.Immutable;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.StoppingThreshold;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.inject.Shareable;

import javax.inject.Inject;
import java.io.Serializable;

@Immutable
@Shareable
public class AbsErrorStoppingCondition implements StoppingCondition, Serializable {
    private static final long serialVersionUID = 5L;

    private final double threshold;

    @Inject
    public AbsErrorStoppingCondition(@StoppingThreshold double thresh) {
        threshold = thresh;
    }

    @Override
    public TrainingLoopController newLoop() {
        return new Controller();
    }


    private class Controller implements TrainingLoopController {
        private int iterations = 0;

        @Override
        public boolean keepTraining(double error) {
            if (error < threshold) {
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
