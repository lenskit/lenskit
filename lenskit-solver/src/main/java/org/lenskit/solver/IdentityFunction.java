package org.lenskit.solver;

import javax.inject.Inject;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class IdentityFunction implements ObjectiveFunction {
    @Inject
    public IdentityFunction() {}

    public void wrapOracle(StochasticOracle orc) {
        orc.objVal = orc.modelOutput * orc.insWeight;
        if (orc.insWeight != 1.0) {
            for (int i=0; i<orc.scalarGrads.size(); i++) {
                orc.scalarGrads.set(i, orc.scalarGrads.getDouble(i) * orc.insWeight);
            }
            for (int i=0; i<orc.vectorGrads.size(); i++) {
                orc.vectorGrads.get(i).mapMultiplyToSelf(orc.insWeight);
            }
        }
    };
}
