package org.lenskit.solver.objective;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class L1Regularizer {
    public double getValue(double var) {
        return Math.abs(var);
    }

    public double getSubGradient(double var) {
        if (var > 0) {
            return 1;
        } else if (var < 0) {
            return -1;
        } else {
            return 0; //sub gradient: any one in [-1, 1]
        }
    }
}
