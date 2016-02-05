package org.lenskit.solver.objective;

import java.util.HashMap;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class LearningModel {
    protected HashMap<String, RealVector> scalarVars;
    protected HashMap<String, RealMatrix> vectorVars;

    public LearningModel() {}

    public HashMap<String, RealVector> getScalarVars() {
        return scalarVars;
    }
    public HashMap<String, RealMatrix> getVectorVars() {
        return vectorVars;
    }
    protected RealVector requestScalarVar(String name, int size, double initial, boolean randomize) {
        RealVector var = MatrixUtils.createRealVector(new double[size]);
        if (randomize) {
            RandomInitializer randInit = new RandomInitializer();
            randInit.randInitVector(var);
        } else {
            var.set(initial);
        }
        scalarVars.put(name, var);
        return var;
    }
    protected RealMatrix requestVectorVar(String name, int size, int dim, double initial, boolean randomize) {
        RealMatrix var = MatrixUtils.createRealMatrix(size, dim);
        if (randomize) {
            RandomInitializer randInit = new RandomInitializer();
            randInit.randInitMatrix(var);
        } else {
            if (initial != 0) {
                for (int i=0; i<size; i++) {
                    var.getRowVector(i).set(initial);
                }
            }
        }
        vectorVars.put(name, var);
        return var;
    }

    public abstract LearningInstance getLearningInstance();
    public abstract void startNewIteration();
    public abstract void assignVariables();
    public abstract StochasticOracle getStochasticOracle(LearningInstance ins);
}
