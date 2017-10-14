package org.lenskit.pf;

import net.jcip.annotations.Immutable;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.StoppingThreshold;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.inject.Shareable;

import javax.inject.Inject;

@Immutable
@Shareable
public class RelativeChangeStoppingCondition implements StoppingCondition {

    private final double threshold;

    @Inject
    public RelativeChangeStoppingCondition(@StoppingThreshold double thresh) {
        threshold = thresh;
    }

    @Override
    public TrainingLoopController newLoop() {
        return new Controller();
    }


    private class Controller implements TrainingLoopController {
        private int iterations = 0;
        private double oldError = Double.MAX_VALUE;
        private double relativeDelta = 0;

        @Override
        public boolean keepTraining(double error) {
            relativeDelta = Math.abs(error - oldError) / oldError;
            if (relativeDelta < threshold) {
                return false;
            } else {
                ++iterations;
                oldError = error;
                return true;
            }
        }

        @Override
        public int getIterationCount() {
            return iterations;
        }


        @Override
        public String toString() {
            return String.format("Stop(threshold=%f, current change is %f)", threshold, relativeDelta);
        }
    }
}
