package org.lenskit.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SynchronizedVariableSpace {
    Map<String, RealVector> scalarVars;
    Map<String, List<RealVector>> vectorVars;

    public SynchronizedVariableSpace() {
        scalarVars = new HashMap<>();
        vectorVars = new HashMap<>();
    }

    final public void requestScalarVar(String name, int size, double initial,
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
    final public void requestVectorVar(String name, int size, int dim, double initial,
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

    final public RealVector getScalarVarByName(String name) {
        return null;
    }

    final public int getScalarVarSizeByName(String name) {
        return 0;
    }

    final public void setScalarVarByName(String name, RealVector vars) {

    }

    final public double getScalarVarByNameIndex(String name, int index) {
        return 0.0;
    }

    final public void setScalarVarByNameIndex(String name, int index, double var) {

    }

    final public List<RealVector> getVectorVarByName(String name) {
        return null;
    }

    final public int getVectorVarSizeByName(String name) {
        return 0;
    }

    final public int getVectorVarDimensionByName(String name) {
        return 0;
    }

    final public RealVector getVectorVarByNameIndex(String name, int index) {
        return null;
    }

    final public void setVectorVarByNameIndex(String name, int index, RealVector var) {

    }

    public List<String> getAllScalarVarNames() {
        return null;
    }

    public List<String> getAllVectorVarNames() {
        return null;
    }
}
