package org.lenskit.solver.objective;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import org.lenskit.solver.method;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HingeLoss implements ObjectiveFunction {
    public HingeLoss() { }

    public void wrapOracle(LearningOracle orc) {
        double output = orc.getModelOutput();
        double label = orc.getInstanceLabel();
        if (label == 0) {
            label = -1;
        }
        double loss = 1 - output * label;
        loss = (loss < 0) ? 0 : loss;
        orc.setObjValue(loss);
        DoubleArrayList gradList = orc.getGradients();
        double hingeGrad = (loss == 0) ? 0 : -label;
        for (int i=0; i<gradList.size(); i++) {
            orc.setGradient(i, hingeGrad * gradList.get(i));
        }
    }
}
