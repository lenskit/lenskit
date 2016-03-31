package org.lenskit.solver;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface ObjectiveFunction {
    void wrapOracle(StochasticOracle orc);
}
