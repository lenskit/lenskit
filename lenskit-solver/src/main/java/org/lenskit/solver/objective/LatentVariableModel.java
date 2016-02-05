package org.lenskit.solver.objective;

import org.lenskit.solver.objective.LearningModel;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class LatentVariableModel extends LearningModel {
    public abstract void expectation();
    public abstract void maximization();
}
