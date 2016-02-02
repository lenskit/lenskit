package org.grouplens.lenskit.solver.objective;

import java.io.IOException;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface LatentVariableModel implements LearningModel {
    public void expectation();
    public void maximization();
}
