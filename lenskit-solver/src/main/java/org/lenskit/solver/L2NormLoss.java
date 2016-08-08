package org.lenskit.solver;

import javax.inject.Inject;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class L2NormLoss implements ObjectiveFunction {
    @Inject
    public L2NormLoss() { }

    public void wrapOracle(StochasticOracle orc) {
        double err = orc.modelOutput - orc.insLabel;
        orc.objVal = err * err * orc.insWeight;
        if (orc.insWeight != 1.0) {
            err *= orc.insWeight;
        }
        for (int i=0; i<orc.scalarGrads.size(); i++) {
            orc.scalarGrads.set(i, orc.scalarGrads.getDouble(i) * err);
        }
        for (int i=0; i<orc.vectorGrads.size(); i++) {
            orc.vectorGrads.get(i).mapMultiplyToSelf(err);
        }
    }
}
