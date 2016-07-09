package org.lenskit.solver;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface OptimizationMethod {
    double minimize(LearningModel model, LearningData learningData, LearningData validData);
}
