package org.lenskit.solver.objective;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class StochasticOracle { 
    private ArrayList<String> scalarNames;
    private DoubleArrayList scalarGrad;
    private IntArrayList scalarIndexes;
    private ArrayList<String> 
    private ArrayList<RealVector> vectorGrads;
    private IntArrayList vectorIndexes;

    private double objval;

    public LearningOracle() {
        scalarIndexes = new IntArrayList();
        scalarGrad = new DoubleArrayList();
        vectorIndexes = new IntArrayList();
        vectorGrads = new ArrayList<RealVector>();
    }

    public addScalarOracle(String name, int index, double grad) {
        scalarIndexes.add(index);
        scalarNames.add(name);
        scalarGrad.add(grad);
    }

    public addVectorOracle(String name, int index, RealVector grad) {
        vectorIndexes.add(index);
        vectorNames.add(name);
        vectorGrad.add(grad);
    }

    public double getObjValue() {
        return objval;
    }

    public void setObjValue(double newVal) {
        objval = newVal;
    }
}

public class DiscriminativeOracle extends StochasticOracle {
    private double modelOutput;
    private double insLabel;

    public DiscriminativeOracle(double inLabel) {
        objval = 0;
        insLabel = inLabel;
    }

    public double getModelOutput() {
        return modelOutput;
    }

    public void setModelOutput(double output) {
        modelOutput = output;
    }

    public double getInstanceLabel() {
        return insLabel;
    }

    public void setInstanceLabel(double label) {
        insLabel = label;
    }
}
