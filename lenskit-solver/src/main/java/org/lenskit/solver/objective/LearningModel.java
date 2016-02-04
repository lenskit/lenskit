package org.lenskit.solver.objective;

import java.io.IOException;

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
    protected requestScalarVar(String name, int size, double initial, boolean randomize) {
        RealVector var = MatrixUtils.createRealVector(new double[size]);
        if (randomize) {
            //do randomization for var
        } else {
            var.set(initial);
        }
        scalarVars[name] = var;
        return var;
    }
    protected requestVectorVar(String name, int size, int dim, double initial, boolean randomize) {
        RealMatrix var = MatrixUtils.createRealMatrix(size, dim);
        if (randomize) {
            //do randomization for var
        } else {
            var.set(initial);
        }
        vectorVars[name] = var;
        return var;
    }

    abstract LearningInstance getLearningInstance();
    abstract void startNewIteration();
    abstract void assignVariables();
    abstract StochasticOracle getStochasticOracle(LearningInstance ins);
}

public abstract class LearningInstance {
    abstract LearningInstance();
}
