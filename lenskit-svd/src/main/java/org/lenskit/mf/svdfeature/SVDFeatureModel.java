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
    private RealVector biases;
    private RealMatrix factors;
    private int factDim;
    private int numFactors;
    private int numBiases;

    public SVDFeatureModel(int inNumBiases, int inNumFactors, int inFactDim, SVDFeatureInstanceDAO inDao) {
        factDim = inFactDim;
        numBiases = inNumBiases;
        numFactors = inNumFactors;
        dao = inDao;
    }

    public void assignVariables() {
        biases = requestScalarVar("biases", numBiases, 0, false);
        factors = requestVectorVar("factors", numFactors, factDim, 0, true);
    }

    public double predict(SVDFeatureInstance inIns, StochasticOracle outOrc,
                          RealVector outUfactSum, RealVector outIfactSum) {
        double pred = 0.0;
        ArrayList<Feature> gfeas = inIns.getGlobalFeas();
        for (int i=0; i<gfeas.size(); i++) {
            int ind = gfeas.get(i).getIndex();
            double val = gfeas.get(i).getValue();
            if (outOrc != null) {
                outOrc.addScalarOracle("biases", ind, val);
            }
            pred += biases.getEntry(ind) * val;
        }

        outUfactSum.set(0.0);
        ArrayList<Feature> ufeas = inIns.getUserFeas();
        for (int i=0; i<ufeas.size(); i++) {
            int index = ufeas.get(i).getIndex();
            outUfactSum.mapMultiplyToSelf(ufeas.get(i).getValue());
            outUfactSum.combineToSelf(1.0, 1.0, factors.getRowVector(index));
        }

        outUfactSum.set(0.0);
        ArrayList<Feature> ifeas = inIns.getItemFeas();
        for (int i=0; i<ifeas.size(); i++) {
            int index = ifeas.get(i).getIndex();
            outIfactSum.mapMultiplyToSelf(ifeas.get(i).getValue());
            outIfactSum.combineToSelf(1.0, 1.0, factors.getRowVector(index));
        }

        pred += outUfactSum.dotProduct(outIfactSum);
        return pred;
    }

    public SVDFeatureInstance getLearningInstance() {
        SVDFeatureInstance ins;
        try {
            ins = dao.getNextInstance();
        } catch (IOException e) {
            ins = null;
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
        ArrayList<Feature> ufeas = ins.getUserFeas();
        for (int i=0; i<ufeas.size(); i++) {
            orc.addVectorOracle(name, ufeas.get(i).getIndex(),
                    leftGrad.mapMultiply(ufeas.get(i).getValue()));
        }
        ArrayList<Feature> ifeas = ins.getItemFeas();
        for (int i=0; i<ifeas.size(); i++) {
            orc.addVectorOracle(name, ifeas.get(i).getIndex(),
                    rightGrad.mapMultiply(ifeas.get(i).getValue()));
        }

        orc.modelOutput = pred;
        orc.insLabel = ins.getLabel();
        return orc;
    }

    public void startNewIteration() {
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
