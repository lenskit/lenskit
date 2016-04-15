package org.lenskit.space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.solver.RandomInitializer;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SynchronizedVariableSpace {
    private final Map<String, List<Double>> scalarVars = new HashMap<>();
    private final Map<String, List<RealVector>> vectorVars = new HashMap<>();
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    public SynchronizedVariableSpace() {}

    private void setDoubleList(List<Double> doubleList, double value) {
        int size = doubleList.size();
        for (int i=0; i<size; i++) {
            doubleList.set(i, value);
        }
    }

    private void setDoubleList(List<Double> doubleList, RealVector vars) {
        int size = doubleList.size();
        for (int i=0; i<size; i++) {
            doubleList.set(i, vars.getEntry(i));
        }
    }

    private void setDoubleList(double[] newVar, List<Double> var) {
        int size = var.size();
        for (int i=0; i<size; i++) {
            newVar[i] = var.get(i);
        }
    }

    final public void requestScalarVar(String name, int size, double initial,
                                          boolean randomize, boolean normalize) {
        DoubleArrayList var = new DoubleArrayList(size);
        if (randomize) {
            RandomInitializer randInit = new RandomInitializer();
            randInit.randInitDoubleList(var, normalize);
        } else {
            setDoubleList(var, initial);
        }
        writeLock.lock();
        try {
            scalarVars.put(name, var);
        } finally {
            writeLock.unlock();
        }
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
        writeLock.lock();
        try {
            vectorVars.put(name, var);
        } finally {
            writeLock.unlock();
        }
    }

    final public RealVector getScalarVarByName(String name) {
        readLock.lock();
        try {
            List<Double> var = scalarVars.get(name);
            double[] newVar = new double[var.size()];
            setDoubleList(newVar, var);
            return MatrixUtils.createRealVector(newVar);
        } finally {
            readLock.unlock();
        }
    }

    final public int getScalarVarSizeByName(String name) {
        readLock.lock();
        try {
            return scalarVars.get(name).size();
        } finally {
            readLock.unlock();
        }
    }

    final public void setScalarVarByName(String name, RealVector vars) {
        writeLock.lock();
        try {
            setDoubleList(scalarVars.get(name), vars);
        } finally {
            writeLock.unlock();
        }
    }

    final public double getScalarVarByNameIndex(String name, int index) {
        readLock.lock();
        try {
            return scalarVars.get(name).get(index);
        } finally {
            readLock.unlock();
        }
    }

    final public void setScalarVarByNameIndex(String name, int index, double var) {
        writeLock.lock();
        try {
            scalarVars.get(name).set(index, var);
        } finally {
            writeLock.unlock();
        }
    }

    final public List<RealVector> getVectorVarByName(String name) {
        List<RealVector> vars = new ArrayList<>();
        readLock.lock();
        try {
            for (RealVector var : vectorVars.get(name)) {
                vars.add(var.copy());
            }
            return vars;
        } finally {
            readLock.unlock();
        }
    }

    final public int getVectorVarSizeByName(String name) {
        readLock.lock();
        try {
            return vectorVars.get(name).size();
        } finally {
            readLock.unlock();
        }
    }

    final public int getVectorVarDimensionByName(String name) {
        readLock.lock();
        try {
            int varSize = vectorVars.get(name).size();
            if (varSize == 0) {
                return 0;
            } else {
                return vectorVars.get(name).get(0).getDimension();
            }
        } finally {
            readLock.unlock();
        }
    }

    final public RealVector getVectorVarByNameIndex(String name, int index) {
        readLock.lock();
        try {
            return vectorVars.get(name).get(index).copy();
        } finally {
            readLock.unlock();
        }
    }

    final public void setVectorVarByNameIndex(String name, int index, RealVector var) {
        writeLock.lock();
        try {
            vectorVars.get(name).get(index).setSubVector(0, var);
        } finally {
            writeLock.unlock();
        }
    }

    public List<String> getAllScalarVarNames() {
        readLock.lock();
        try {
            return new ArrayList<>(scalarVars.keySet());
        } finally {
            readLock.unlock();
        }
    }

    public List<String> getAllVectorVarNames() {
        readLock.lock();
        try {
            return new ArrayList<>(vectorVars.keySet());
        } finally {
            readLock.unlock();
        }
    }
}
