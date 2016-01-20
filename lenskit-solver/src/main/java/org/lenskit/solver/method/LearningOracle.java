package org.grouplens.lenskit.solver.method;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LearningOracle {
    private double objval;
    private double modelOutput;
    private double insLabel;
    private DoubleArrayList variables;
    private DoubleArrayList gradients;
    private IntArrayList varIndexes;

    public LearningOracle() {
        objval = 0;
        modelOutput = 0;
        variables = new DoubleArrayList();
        gradients = new DoubleArrayList();
        varIndexes = new IntArrayList();
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

    public double getObjValue() {
        return objval;
    }

    public void setObjValue(double newVal) {
        objval = newVal;
    }

    public DoubleArrayList getVariables() {
        return variables;
    }

    public DoubleArrayList getGradients() {
        return gradients;
    }

    public IntArrayList getVarIndexes() {
        return varIndexes;
    }

    public void addVariable(double var) {
        variables.add(var);
    }

    public void addGradient(double grad) {
        gradients.add(grad);
    }

    public void addVarIndex(int varIndex) {
        varIndexes.add(varIndex);
    }

    public void setVariable(int index, double var) {
        variables.set(index, var);
    }

    public void setGradient(int index, double grad) {
        gradients.set(index, grad);
    }

    public void setVarIndex(int index, int varInd) {
        varIndexes.set(index, varInd);
    }
}
