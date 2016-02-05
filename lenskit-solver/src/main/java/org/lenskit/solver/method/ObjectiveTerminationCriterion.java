package org.lenskit.solver.method;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ObjectiveTerminationCriterion {
    private static Logger logger = LoggerFactory.getLogger(StochasticGradientDescent.class);
    private int maxIter;
    private int curIter;
    private double tol;
    private DoubleArrayList objHistory;

    public ObjectiveTerminationCriterion(double outTol, int outMaxIter) {
        maxIter = outMaxIter;
        tol = outTol;
        curIter = 0;
        objHistory = new DoubleArrayList();
    }

    public void addIteration(double objVal) {
        curIter++;
        objHistory.add(objVal);
        logger.info("Iteration {}: objective value is {}", curIter, objVal);
    }

    public boolean keepIterate() {
        if (curIter < 2) {
            return true;
        } else if (curIter >= maxIter) {
            return false;
        } else if (objHistory.get(curIter - 2) - objHistory.get(curIter - 1) < tol) {
            return false;
        } else {
            return true;
        }
    }
}
