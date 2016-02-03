package org.lenskit.solver.objective;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class LearningOracle { 
    private DoubleArrayList scalarGrad;
    private IntArrayList scalarVarIndexes;
    private ArrayList<RealVector> vectorGrads;
    private IntArrayList vectorVarIndexes;

    private double objval;

    public LearningOracle() {
        scalarVarIndexes = new IntArrayList();
        scalarVars = new DoubleArrayList();
        scalarGrad = new DoubleArrayList();
        vectorVarIndexes = new IntArrayList();
        vectorVars = new ArrayList<RealVector>();
        vectorGrads = new ArrayList<RealVector>();
    }

    public double getObjValue() {
        return objval;
    }

    public void setObjValue(double newVal) {
        objval = newVal;
    }
}

public class DiscriminativeOracle extends LearningOracle {
    private double modelOutput;
    private double insLabel;

    public DiscriminativeOracle() {
        objval = 0;
        modelOutput = 0;
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
