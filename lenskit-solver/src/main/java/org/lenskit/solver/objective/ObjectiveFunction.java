package org.grouplens.lenskit.solver.objective;

import org.grouplens.lenskit.solver.method;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface ObjectiveFunction {
    public void wrapOracle(LearningOracle orc);
}
