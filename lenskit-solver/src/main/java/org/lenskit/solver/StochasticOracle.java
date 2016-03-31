package org.lenskit.solver;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;

public class StochasticOracle { 
    List<String> scalarNames;
    IntArrayList scalarIndexes;
    DoubleArrayList scalarGrads;
    List<String> vectorNames;
    IntArrayList vectorIndexes;
    List<RealVector> vectorGrads;

    public double objVal;
    public double modelOutput;
    public double insLabel;
    public double insWeight;

    public StochasticOracle() {
        scalarNames = new ArrayList<>();
        scalarIndexes = new IntArrayList();
        scalarGrads = new DoubleArrayList();
        vectorNames = new ArrayList<>();
        vectorIndexes = new IntArrayList();
        vectorGrads = new ArrayList<>();
    }

    public void addScalarOracle(String name, int index, double grad) {
        scalarIndexes.add(index);
        scalarNames.add(name);
        scalarGrads.add(grad);
    }

    public void addVectorOracle(String name, int index, RealVector grad) {
        vectorIndexes.add(index);
        vectorNames.add(name);
        vectorGrads.add(grad);
    }
}
