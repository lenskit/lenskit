package org.grouplens.lenskit.solver;

import java.util.ArrayList;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ObjectiveTerminationCriterion {
    private int maxIter;
    private int curIter;
    private double tol;
    private ArrayList<Double> objHistory;

    public ObjectiveTerminationCriterion(double outTol, int outMaxIter) {
        maxIter = outMaxIter;
        tol = outTol;
        curIter = 0;
        objHistory = new ArrayList<Double>();
    }

    public void addIteration(double objval) {
        curIter++;
        objHistory.add(objval);
        System.out.println(Integer.toString(curIter) + ": " + Double.toString(objval));
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
