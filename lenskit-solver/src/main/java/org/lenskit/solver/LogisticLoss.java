package org.lenskit.solver;

import javax.inject.Inject;

public class LogisticLoss implements ObjectiveFunction {
    @Inject
    public LogisticLoss() { }

    //unused
    static public double sigmoid(double y) {
        if (y < -30.0) {
            return 0.001;
        } else if (y > 30.0) {
            return 0.999;
        } else {
            return 1.0 / (1.0 + Math.exp(-y));
        }
    }

    public void wrapOracle(StochasticOracle orc) {
        orc.objVal = orc.insWeight * (Math.log(1.0 + Math.exp(orc.modelOutput)) - orc.insLabel * orc.modelOutput);
        double err = 1.0 / (1.0 + Math.exp(-orc.modelOutput)) - orc.insLabel;
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
