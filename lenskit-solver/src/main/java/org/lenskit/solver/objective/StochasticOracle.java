package org.lenskit.solver.objective;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;

public class StochasticOracle { 
    public ArrayList<String> scalarNames;
    public IntArrayList scalarIndexes;
    public DoubleArrayList scalarGrads;
    public ArrayList<String> vectorNames;
    public IntArrayList vectorIndexes;
    public ArrayList<RealVector> vectorGrads;

    public double objVal;
    public double modelOutput;
    public double insLabel;

    public StochasticOracle() {
        scalarNames = new ArrayList<String>();
        scalarIndexes = new IntArrayList();
        scalarGrads = new DoubleArrayList();
        vectorNames = new ArrayList<String>();
        vectorIndexes = new IntArrayList();
        vectorGrads = new ArrayList<RealVector>();
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
