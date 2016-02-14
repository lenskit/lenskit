package org.lenskit.solver.objective;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class LearningModel implements Serializable {
    protected HashMap<String, RealVector> scalarVars;
    protected HashMap<String, ArrayList<RealVector>> vectorVars;

    public LearningModel() {
        scalarVars = new HashMap<>();
        vectorVars = new HashMap<>();
    }

    public HashMap<String, RealVector> getScalarVars() {
        return scalarVars;
    }
    public HashMap<String, ArrayList<RealVector>> getVectorVars() {
        return vectorVars;
    }
    protected RealVector requestScalarVar(String name, int size, double initial, 
                                          boolean randomize, boolean normalize) {
        RealVector var = MatrixUtils.createRealVector(new double[size]);
        if (randomize) {
            RandomInitializer randInit = new RandomInitializer();
            randInit.randInitVector(var, normalize);
        } else {
            var.set(initial);
        }
        scalarVars.put(name, var);
        return var;
    }
    protected ArrayList<RealVector> requestVectorVar(String name, int size, int dim, double initial,
                                          boolean randomize, boolean normalize) {
        ArrayList<RealVector> var = new ArrayList<>(size);
        for (int i=0; i<size; i++) {
            RealVector vec = MatrixUtils.createRealVector(new double[dim]);
            if (randomize) {
                RandomInitializer randInit = new RandomInitializer();
                randInit.randInitVector(vec, normalize);
            } else {
                if (initial != 0) {
                    vec.set(initial);
                }
            }
            var.add(vec);
        }
        vectorVars.put(name, var);
        return var;
    }

    public LearningInstance getLearningInstance() { return null; }
    public void startNewIteration() {}
    public void assignVariables() {}
    public StochasticOracle getStochasticOracle(LearningInstance ins) { return null; }

    public double expectation(LearningInstance ins) { return 0.0; }
    public double stochasticExpectation(LearningInstance ins) { return 0.0; }
    public LearningModel maximization() { return null; }
    public LearningModel stochasticMaximization() { return null; }
}
