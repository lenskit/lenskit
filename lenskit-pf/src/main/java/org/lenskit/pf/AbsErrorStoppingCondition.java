package org.lenskit.pf;

import net.jcip.annotations.Immutable;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.StoppingThreshold;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.inject.Shareable;

import javax.inject.Inject;

@Immutable
@Shareable
public class AbsErrorStoppingCondition implements StoppingCondition {

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
