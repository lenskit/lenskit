package org.lenskit.solver;

import java.io.Serializable;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface ObjectiveFunction extends Serializable {
    void wrapOracle(StochasticOracle orc);
}
