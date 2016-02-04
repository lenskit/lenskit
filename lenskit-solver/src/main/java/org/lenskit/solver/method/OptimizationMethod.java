package org.lenskit.solver.method;

import java.io.IOException;

import org.lenskit.objective.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface OptimizationMethod {
    public void minimize(LearningModel model, ObjectiveFunction objFunc);
}
