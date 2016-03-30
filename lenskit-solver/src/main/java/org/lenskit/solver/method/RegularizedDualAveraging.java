package org.lenskit.solver.method;

import org.lenskit.solver.objective.LearningModel;
import org.lenskit.solver.objective.ObjectiveFunction;

public class RegularizedDualAveraging implements OptimizationMethod {
    public double minimize(LearningModel learningModel, ObjectiveFunction objectiveFunction) {
        return 0.0;
    }
}
