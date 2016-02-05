package org.lenskit.solver.objective;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import org.lenskit.solver.method;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HingeLoss implements ObjectiveFunction {
    public HingeLoss() { }

    public void wrapOracle(StochasticOracle orc) {
        double label = orc.insLabel;
        if (label == 0) {
            label = -1;
        }
        double loss = 1 - orc.modelOutput * label;
        orc.objVal = (loss < 0) ? 0 : loss;
        double hingeGrad = (loss == 0) ? 0 : -label;
        for (int i=0; i<orc.scalarGrads.size(); i++) {
            orc.scalarGrads.set(i, orc.scalarGrads.getDouble(i) * hingeGrad);
        }
        for (int i=0; i<orc.vectorGrads.size(); i++) {
            orc.vectorGrads.get(i).mapMultiplyToSelf(hingeGrad);
        }
    }
}
