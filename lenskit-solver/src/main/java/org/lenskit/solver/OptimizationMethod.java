package org.lenskit.solver;

import org.grouplens.grapht.annotation.DefaultImplementation;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultImplementation(StochasticGradientDescent.class)
public interface OptimizationMethod {
    double minimize(LearningModel model, LearningData learningData, LearningData validData);
}
