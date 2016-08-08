package org.lenskit.solver;

import org.grouplens.grapht.annotation.DefaultImplementation;

import java.io.Serializable;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultImplementation(L2NormLoss.class)
public interface ObjectiveFunction extends Serializable {
    void wrapOracle(StochasticOracle orc);
}
