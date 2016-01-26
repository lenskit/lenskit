package org.grouplens.lenskit.solver.objective;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class L2Regularizer {
    public double getValue(double var) {
        return var * var;
    }

    public double getGradient(double var) {
        return 2 * var;
    }
}
