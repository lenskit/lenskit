package org.lenskit.solver.method;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class StochasticOracle { 
    protected ArrayList<String> scalarNames;
    protected DoubleArrayList scalarGrad;
    protected IntArrayList scalarIndexes;
    protected ArrayList<String> vectorNames;
    protected ArrayList<RealVector> vectorGrads;
    protected IntArrayList vectorIndexes;

    protected double objval;

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
    protected double modelOutput;
    protected double insLabel;

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
