package org.lenskit.solver.objective;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import org.lenskit.solver.method;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class L2NormLoss implements ObjectiveFunction {
    public L2NormLoss() { }

    public void wrapOracle(StochasticOracle orc) {
        double err = orc.modelOutput - orc.insLabel;
        orc.objVal = err * err;
        for (int i=0; i<orc.scalarGrads.size(); i++) {
            orc.scalarGrads.set(i, orc.scalarGrads.getDouble(i) * err);
        }
        for (int i=0; i<orc.vectorGrads.size(); i++) {
            orc.vectorGrads.get(i).mapMultiplyToSelf(err);
        }
    }
}
