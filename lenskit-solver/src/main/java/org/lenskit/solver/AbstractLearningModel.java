package org.lenskit.solver;

import org.apache.commons.math3.linear.RealVector;
import org.lenskit.space.SynchronizedIndexSpace;
import org.lenskit.space.SynchronizedVariableSpace;

import java.util.List;

abstract public class AbstractLearningModel implements LearningModel {
    final protected SynchronizedVariableSpace variableSpace = new SynchronizedVariableSpace();
    protected SynchronizedIndexSpace indexSpace = new SynchronizedIndexSpace();

    protected AbstractLearningModel() {}

    public RealVector getScalarVarByName(String name) {
        return variableSpace.getScalarVarByName(name);
    }

    public int getScalarVarSizeByName(String name) {
        return variableSpace.getScalarVarSizeByName(name);
    }

    public void setScalarVarByName(String name, RealVector vars) {
        variableSpace.setScalarVarByName(name, vars);
    }

    public double getScalarVarByNameIndex(String name, int index) {
        return variableSpace.getScalarVarByNameIndex(name, index);
    }

    public void setScalarVarByNameIndex(String name, int index, double var) {
        variableSpace.setScalarVarByNameIndex(name, index, var);
    }

    public List<RealVector> getVectorVarByName(String name) {
        return variableSpace.getVectorVarByName(name);
    }

    public int getVectorVarSizeByName(String name) {
        return variableSpace.getVectorVarSizeByName(name);
    }

    public int getVectorVarDimensionByName(String name) {
        return variableSpace.getVectorVarDimensionByName(name);
    }

    public RealVector getVectorVarByNameIndex(String name, int index) {
        return variableSpace.getVectorVarByNameIndex(name, index);
    }

    public void setVectorVarByNameIndex(String name, int index, RealVector var) {
        variableSpace.setVectorVarByNameIndex(name, index, var);
    }

    public List<String> getAllScalarVarNames() {
        return variableSpace.getAllScalarVarNames();
    }

    public List<String> getAllVectorVarNames() {
        return variableSpace.getAllVectorVarNames();
    }
}
