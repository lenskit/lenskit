package org.lenskit.solver.method;

import org.lenskit.solver.objective.LearningModel;
import org.lenskit.solver.objective.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface OptimizationMethod {
    public void minimize(LearningModel model, ObjectiveFunction objFunc);
}
