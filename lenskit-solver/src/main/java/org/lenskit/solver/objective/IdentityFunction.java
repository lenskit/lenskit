package org.lenskit.solver.objective;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class IdentityFunction {
    public IdentityFunction() {}
    public void wrapOracle(StochasticOracle orc) {
        orc.objVal = orc.modelOutput;
    };
}
