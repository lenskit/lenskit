package org.lenskit.solver;

import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class L2Regularizer {
    public L2Regularizer() {}

    public double getValue(double var) {
        return var * var;
    }

    public double getGradient(double var) {
        return 2 * var;
    }

    public RealVector addGradient(RealVector grad, RealVector var, double l2coef) {
        return grad.combineToSelf(1.0, 2 * l2coef, var);
    }

    public double getObjective(double l2coef, RealVector var) {
        double l2norm = var.getNorm();
        return l2coef * l2norm * l2norm;
    }

    public double getObjective(double l2coef, List<RealVector> vars) {
        double objVal = 0.0;
        for (RealVector realVector : vars) {
            double l2norm = realVector.getNorm();
            objVal += l2norm * l2norm;
        }
        return objVal * l2coef;
    }
}
