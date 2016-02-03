package org.lenskit.mf.svdfeature;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealMatrix;

import org.lenskit.solver.objective.LearningModel;
import org.lenskit.solver.objective.LearningOracle;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModel implements LearningModel {
    private SVDFeatureInstanceDAO dao;
    private RealVector biases;
    private RealMatrix factors;
    private int factDim;
    private int numFactors;
    private int numBiases;
    private KernelFunction kernel;
    private boolean acceptOutOfRangeIdx;

    public SVDFeatureModel(int inNumBiases, int inNumFactors, int infactDim,
                           SVDFeatureInstanceDAO inDao, KernelFunction inKernel) {
        factDim = infactDim;
        numBiases = inNumBiases;
        numFactors = inNumFactors;
        factors = MatrixUtils.createRealMatrix(numFactors, factDim);
        biases = MatrixUtils.createRealVector(new double[numBiases]);
        dao = inDao;
        kernel = inKernel;
        acceptOutOfRangeIdx = false;
    }

    public void randomInitialize() {
    }

    public int getNumOfAlternation() {
        return 2;
    }

    public int getScalarVarNum() {
        return numBiases;
    }

    public int getVectorVarNum() {
        return numFactors;
    }

    public double predict(SVDFeatureInstance inIns, LearningOracle outOrc,
                          RealVector outUfactSum, RealVector outIfactSum) {
        double pred = 0.0;
        ArrayList<Feature> gfeas = inIns.getGlobalFeas();
        for (int i=0; i<gfeas.size(); i++) {
            int ind = gfeas.get(i).getIndex();
            double val = gfeas.get(i).getValue();
            double var = biases.getEntry(ind);
            if (outOrc != null) {
                outOrc.addScalarVar(ind, var, val);
            }
            pred += var * val;
        }

        outUfactSum = MatrixUtils.createRealVector(new double[factDim]);
        outUfactSum.set(0.0);
        ArrayList<Feature> ufeas = inIns.getUserFeas();
        for (int i=0; i<ufeas.size(); i++) {
            int index = ufeas.get(i).getIndex();
            outUfactSum.mapMultiplyToSelf(ufeas.get(i).getValue());
            ArrayHelper.addition(outUfactSum, factors.getRowVector(index));
        }

        outIfactSum = MatrixUtils.createRealVector(new double[factDim]);
        outUfactSum.set(0.0);
        ArrayList<Feature> ifeas = inIns.getItemFeas();
        for (int i=0; i<ifeas.size(); i++) {
            int index = ifeas.get(i).getIndex();
            outIfactSum.mapMultiplyToSelf(ifeas.get(i).getValue());
            ArrayHelper.addition(outIfactSum, factors.getRowVector(index));
        }

        pred += kernel.getValue(outUfactSum, outIfactSum);
        return pred;
    }

    public LearningOracle getStochasticOracle() throws IOException {
        SVDFeatureInstance ins = dao.getNextInstance();
        if (ins == null) {
            return null;
        }

        LearningOracle orc = new LearningOracle();
        RealVector ufactSum, ifactSum;
        double pred = predict(ins, orc, ufactSum, ifactSum);
    
        RealVector leftGrad, rightGrad;
        kernel.getGradient(ufactSum, ifactSum, leftGrad, rightGrad);
        ArrayList<Feature> ufeas = inIns.getUserFeas();
        for (int i=0; i<ufeas.size(); i++) {
            int index = ufeas.get(i).getIndex();
            orc.addVectorVar(index, factors.getRowVector(index), 
                    leftGrad.mapMultiply(ufeas.get(i).getValue()));
        }
        ArrayList<Feature> ifeas = inIns.getItemFeas();
        for (int i=0; i<ifeas.size(); i++) {
            int index = ifeas.get(i).getIndex();
            orc.addVectorVar(index, factors.getRowVector(index), 
                    leftGrad.mapMultiply(ifeas.get(i).getValue()));
        }

        orc.setModelOutput(pred);
        orc.setInstanceLabel(ins.getLabel());
        return orc;
    }

    public LearningOracle getNextAlternatingOracle(int k) throws IOException {
        LearningOracle orc = new LearningOracle();
        return orc;
    }

    public void startNewIteration() throws IOException {
        dao.goBackToBeginning();
    }

    public int getFactDim() {
        return factDim;
    }

    public double predict(SVDFeatureInstance ins, boolean sigmoid) {
        RealVector ufactSum, ifactSum;
        double pred = predict(ins, null, ufactSum, ifactSum);
        if (sigmoid) {
            return 1 / (1 + Math.exp(-pred)); //define a sigmoid function for cases pred is very big or small
        } else {
            return pred;
        }
    }
}
