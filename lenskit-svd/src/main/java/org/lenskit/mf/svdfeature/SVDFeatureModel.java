package org.lenskit.mf.svdfeature;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealMatrix;

import org.lenskit.solver.objective.LearningInstance;
import org.lenskit.solver.objective.LearningModel;
import org.lenskit.solver.objective.StochasticOracle;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModel extends LearningModel {
    private SVDFeatureInstanceDAO dao;
    private ArrayList<SVDFeatureInstance> instances;
    private RealVector biases;
    private RealMatrix factors;
    private int factDim;
    private int numFactors;
    private int numBiases;
    private int insIdx;

    public SVDFeatureModel(int inNumBiases, int inNumFactors, int inFactDim, SVDFeatureInstanceDAO inDao) {
        factDim = inFactDim;
        numBiases = inNumBiases;
        numFactors = inNumFactors;
        dao = inDao;
        instances = null;
        insIdx = 0;
    }

    public SVDFeatureModel(int inNumBiases, int inNumFactors, int inFactDim) {
        factDim = inFactDim;
        numBiases = inNumBiases;
        numFactors = inNumFactors;
        dao = null;
        instances = null;
        insIdx = 0;
    }

    public setInstances(ArrayList<SVDFeatureInstance> outIns) {
        instances = outIns;
    }

    public void assignVariables() {
        biases = requestScalarVar("biases", numBiases, 0, false, false,);
        factors = requestVectorVar("factors", numFactors, factDim, 0, true, false);
    }

    public double predict(SVDFeatureInstance ins, StochasticOracle outOrc,
                          RealVector outUfactSum, RealVector outIfactSum) {
        double pred = 0.0;
        for (int i=0; i<ins.gfeas.size(); i++) {
            int ind = ins.gfeas.get(i).index;
            double val = ins.gfeas.get(i).value;
            if (outOrc != null) {
                outOrc.addScalarOracle("biases", ind, val);
            }
            pred += biases.getEntry(ind) * val;
        }

        outUfactSum.set(0.0);
        for (int i=0; i<ins.ufeas.size(); i++) {
            int index = ins.ufeas.get(i).index;
            outUfactSum.mapMultiplyToSelf(ins.ufeas.get(i).value);
            outUfactSum.combineToSelf(1.0, 1.0, factors.getRowVector(index));
        }

        outUfactSum.set(0.0);
        for (int i=0; i<ins.ifeas.size(); i++) {
            int index = ins.ifeas.get(i).index;
            outIfactSum.mapMultiplyToSelf(ins.ifeas.get(i).value);
            outIfactSum.combineToSelf(1.0, 1.0, factors.getRowVector(index));
        }

        pred += outUfactSum.dotProduct(outIfactSum);
        return pred;
    }

    public SVDFeatureInstance getLearningInstance() {
        SVDFeatureInstance ins = null;
        if (dao != null) {
            try {
                ins = dao.getNextInstance();
            } catch (IOException e) {
                ins = null;
            }
        } else if (instances.size() > insIdx) {
            ins = instances.get(insIdx);
            ++insIdx;
        }
        return ins;
    }

    public StochasticOracle getStochasticOracle(LearningInstance inIns) {
        SVDFeatureInstance ins;
        if (inIns instanceof SVDFeatureInstance) {
            ins = (SVDFeatureInstance) inIns;
        } else {
            return null;
        }
        StochasticOracle orc = new StochasticOracle();
        RealVector ufactSum = MatrixUtils.createRealVector(new double[factDim]);
        RealVector ifactSum = MatrixUtils.createRealVector(new double[factDim]);
        double pred = predict(ins, orc, ufactSum, ifactSum);
   
        RealVector leftGrad = ifactSum;
        RealVector rightGrad = ufactSum;
        String name = "factors";
        for (int i=0; i<ins.ufeas.size(); i++) {
            orc.addVectorOracle(name, ins.ufeas.get(i).index;
                    leftGrad.mapMultiply(ins.ufeas.get(i).value));
        }
        for (int i=0; i<ins.ifeas.size(); i++) {
            orc.addVectorOracle(name, ins.ifeas.get(i).index;
                    rightGrad.mapMultiply(ins.ifeas.get(i).value));
        }

        orc.modelOutput = pred;
        orc.insLabel = ins.label;
        orc.insWeight = ins.weight;
        return orc;
    }

    public void startNewIteration() {
        insIdx = 0;
        try {
            dao.goBackToBeginning();
        } catch (IOException e) {}
    }

    public double predict(SVDFeatureInstance ins, boolean sigmoid) {
        RealVector ufactSum = MatrixUtils.createRealVector(new double[factDim]);
        RealVector ifactSum = MatrixUtils.createRealVector(new double[factDim]);
        double pred = predict(ins, null, ufactSum, ifactSum);
        if (sigmoid) {
            return 1 / (1 + Math.exp(-pred)); 
        } else {
            return pred;
        }
    }
}
