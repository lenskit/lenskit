package org.grouplens.lenskit.solver.method;

import java.io.IOException;

import org.grouplens.lenskit.objective.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface OptimizationMethod {
    public void minimize(LearningModel model, ObjectiveFunction objFunc, double tol, int maxIter,
                         double l1coef, double l2coef, double learningRate) throws IOException;
}
