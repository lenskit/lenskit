package org.lenskit.solver.objective;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import org.lenskit.solver.method;

public class NegativeLogLikelihoodLoss implements ObjectiveFunction {
    public NegativeLogLikelihoodLoss() { }

    public void wrapOracle(LearningOracle orc) {
        double output = orc.getModelOutput();
        double label = orc.getInstanceLabel();
        orc.setObjValue(Math.log(1 + Math.exp(output)) - label * output);
        DoubleArrayList gradList = orc.getGradients();
        for (int i=0; i<gradList.size(); i++) {
            orc.setGradient(i, gradList.get(i) * Math.exp(output) / (1 + Math.exp(output)) - 
                            label * gradList.get(i));
        }
    }
}
