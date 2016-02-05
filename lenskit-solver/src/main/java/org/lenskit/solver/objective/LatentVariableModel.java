package org.grouplens.lenskit.solver.objective;

import java.io.IOException;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class LatentVariableModel extends LearningModel {
    public abstract void expectation();
    public abstract void maximization();
}
