package org.lenskit.solver.objective;

import org.lenskit.solver.method;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface ObjectiveFunction {
    public void wrapOracle(StochasticOracle orc);
}
