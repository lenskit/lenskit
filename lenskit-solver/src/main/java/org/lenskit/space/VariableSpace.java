package org.lenskit.space;

import org.apache.commons.math3.linear.RealVector;

import java.util.List;

public interface VariableSpace {
    void requestScalarVar(String name, int size, double initial,
                                       boolean randomize, boolean normalize);
    void ensureScalarVar(String name, int size, double initial, boolean randomize);
    void requestVectorVar(String name, int size, int dim, double initial,
                                       boolean randomize, boolean normalize);
    void ensureVectorVar(String name, int size, int dim, double initial,
                                      boolean randomize, boolean normalize);
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
}
