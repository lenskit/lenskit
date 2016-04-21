package org.lenskit.solver;

import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;
import java.util.List;

public interface LearningModel extends Serializable {
    RealVector getScalarVarByName(String name);
    int getScalarVarSizeByName(String name);
    void setScalarVarByName(String name, RealVector vars);
    double getScalarVarByNameIndex(String name, int index);
    void setScalarVarByNameIndex(String name, int index, double var);

    List<RealVector> getVectorVarByName(String name);
    int getVectorVarSizeByName(String name);
    int getVectorVarDimensionByName(String name);
    RealVector getVectorVarByNameIndex(String name, int index);
    void setVectorVarByNameIndex(String name, int index, RealVector var);

    List<String> getAllScalarVarNames();
    List<String> getAllVectorVarNames();

    StochasticOracle getStochasticOracle(LearningInstance ins);
    ObjectiveFunction getObjectiveFunction();
}
