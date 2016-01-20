package org.grouplens.lenskit.solver.objective;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import org.grouplens.lenskit.solver.method;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class L2NormLoss implements ObjectiveFunction {
    public L2NormLoss() { }

    public void wrapOracle(LearningOracle orc) {
        double output = orc.getModelOutput();
        double label = orc.getInstanceLabel();
        double err = output - label;
        orc.setObjValue(err * err);
        DoubleArrayList gradList = orc.getGradients();
        for (int i=0; i<gradList.size(); i++) {
            orc.setGradient(i, err * gradList.get(i));
        }
    }
}
