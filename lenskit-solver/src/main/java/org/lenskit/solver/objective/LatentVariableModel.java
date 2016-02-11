package org.lenskit.solver.objective;

import org.lenskit.solver.objective.LearningModel;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class LatentVariableModel extends LearningModel {
    //do closed form maximization in the expectation or stochastic_expectation step
    //and return the corresponding part of the objective value
    public double expectation(LearningInstance ins);
    public double stochastic_expectation(LearningInstance ins);
    public LearningModel maximization() {
        return null;    
    };
    public LearningModel stochastic_maximization() {
        return null;    
    };
}
