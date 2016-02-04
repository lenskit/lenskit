package org.lenskit.solver.objective;

import org.lenskit.solver.method;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class IdentityFunction {
    public IdentityFunction() {}
    public void wrapOracle(StochasticOracle orc) {
        orc.setObjValue(orc.getModelOutput());
    };
}
