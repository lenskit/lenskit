package org.lenskit.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SynchronizedVariableSpace {
    HashMap<String, RealVector> scalarVars;
    HashMap<String, List<RealVector>> vectorVars;

    public SynchronizedVariableSpace() {
        scalarVars = new HashMap<>();
        vectorVars = new HashMap<>();
    }

    public double getScalarVar(String name, int index) {
        return scalarVars.get(name).getEntry(index);
    }

    void setScalarVar(String name, int index, double val) {
        scalarVars.get(name).setEntry(index, val);
    }

    //make sure returned the RealVector is defensively copied
    public RealVector getVectorVar(String name, int index) {
        return vectorVars.get(name).get(index);
    }

    void setVectorVar(String name, int index, RealVector val) {
    }

    public void requestScalarVar(String name, int size, double initial,
                                          boolean randomize, boolean normalize) {
        RealVector var = MatrixUtils.createRealVector(new double[size]);
        if (randomize) {
            RandomInitializer randInit = new RandomInitializer();
            randInit.randInitVector(var, normalize);
        } else {
            var.set(initial);
        }
        scalarVars.put(name, var);
    }
    public void requestVectorVar(String name, int size, int dim, double initial,
                                          boolean randomize, boolean normalize) {
        List<RealVector> var = new ArrayList<>(size);
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
    }
}
