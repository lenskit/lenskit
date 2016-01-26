package org.grouplens.lenskit.mf.svdfeature;

import java.io.IOException;
import java.util.ArrayList;

import org.grouplens.lenskit.solver.objective.LearningModel;
import org.grouplens.lenskit.solver.objective.LearningOracle;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModel implements LearningModel {
    private SVDFeatureInstanceDAO dao;
    private DenseMatrix gbiases;
    private DenseMatrix ufacts;
    private DenseMatrix ifacts;
    private int factDim;
    private int numUserFeas;
    private int numItemFeas;
    private int numGlobalFeas;
    private KernelFunction kernel;

    public SVDFeatureModel(int inNumGlobalFeas, int inNumUserFeas, int inNumItemFeas, int dim,
                           SVDFeatureInstanceDAO inDao, KernelFunction inKernel) {
        factDim = dim;
        numGlobalFeas = inNumGlobalFeas;
        numUserFeas = inNumUserFeas;
        numItemFeas = inNumItemFeas;
        gbiases = new DenseMatrix(numGlobalFeas, 1);
        ufacts = new DenseMatrix(numUserFeas, factDim);
        ifacts = new DenseMatrix(numItemFeas, factDim);
        dao = inDao;
        kernel = inKernel;
        //randomInitialize();
    }

    public void randomInitialize() {
        for (int i=0; i<numGlobalFeas; i++) {
            gbiases.setEntry(i, 0, Math.random());
        }
        for (int i=0; i<numUserFeas; i++) {
            for (int j=0; j<factDim; j++) {
                ufacts.setEntry(i, j, Math.random());
            }
        }
        for (int i=0; i<numItemFeas; i++) {
            for (int j=0; j<factDim; j++) {
                ifacts.setEntry(i, j, Math.random());
            }
        }
    }

    public int getNumOfAlternation() {
        return 2;
    }

    public int getNumOfVariables() {
        return numGlobalFeas + factDim * (numUserFeas + numItemFeas);
    }

    public int getNumOfGlobalFeas() {
        return numGlobalFeas;
    }

    public int getNumOfUserFeas() {
        return numUserFeas;
    }

    public int getNumOfItemFeas() {
        return numItemFeas;
    }

    public double getGlobalBias(int index) {
        return gbiases.getEntry(index, 0);
    }

    public double[] getUserFeaFacts(int index) {
        return ufacts.getRow(index);
    }

    public double[] getItemFeaFacts(int index) {
        return ifacts.getRow(index);
    }

    public LearningOracle getNextOracle() throws IOException {
        SVDFeatureInstance ins = dao.getNextInstance();
        if (ins == null) {
            return null;
        }
        double output = 0;
        LearningOracle orc = new LearningOracle();

        orc.setInstanceLabel(ins.getLabel());
        ArrayList<Feature> gfeas = ins.getGlobalFeas();
        for (int i=0; i<gfeas.size(); i++) {
            int ind = gfeas.get(i).getIndex();
            output += gbiases.getEntry(ind, 0) * gfeas.get(i).getValue();
            orc.addVarIndex(ind);
            orc.addVariable(gbiases.getEntry(ind, 0));
            orc.addGradient(gfeas.get(i).getValue());
        }

        ArrayList<Feature> ufeas = ins.getUserFeas();
        double[] ufactSum = new double[factDim];
        ArrayHelper.initialize(ufactSum, 0);
        for (int i=0; i<ufeas.size(); i++) {
            int index = ufeas.get(i).getIndex();
            int ind = getUserFeaFactIndex(index);
            double[] feaFact = ufacts.getRow(index);
            for (int j=0; j<factDim; j++) {
                orc.addVarIndex(ind + j);
                orc.addVariable(feaFact[j]);
            }
            ArrayHelper.scale(feaFact, ufeas.get(i).getValue());
            ArrayHelper.addition(ufactSum, feaFact);
        }

        ArrayList<Feature> ifeas = ins.getItemFeas();
        double[] ifactSum = new double[factDim];
        ArrayHelper.initialize(ifactSum, 0);
        for (int i=0; i<ifeas.size(); i++) {
            int index = ifeas.get(i).getIndex();
            int ind = getItemFeaFactIndex(index);
            double[] feaFact = ifacts.getRow(index);
            for (int j=0; j<factDim; j++) {
                orc.addVarIndex(ind + j);
                orc.addVariable(feaFact[j]);
            }
            ArrayHelper.scale(feaFact, ifeas.get(i).getValue());
            ArrayHelper.addition(ifactSum, feaFact);
        }

        double[] kernelGrad = kernel.getGradient(ufactSum, ifactSum, true);
        for (int i=0; i<ufeas.size(); i++) {
            for (int j=0; j<factDim; j++) {
                orc.addGradient(kernelGrad[j] * ufeas.get(i).getValue());
            }
        }
        
        kernelGrad = kernel.getGradient(ufactSum, ifactSum, false);
        for (int i=0; i<ifeas.size(); i++) {
            for (int j=0; j<factDim; j++) {
                orc.addGradient(kernelGrad[j] * ifeas.get(i).getValue());
            }
        }

        output += kernel.getValue(ufactSum, ifactSum);
        orc.setModelOutput(output);
        orc.setInstanceLabel(ins.getLabel());
        return orc;
    }

    public LearningOracle getNextAlternatingOracle(int k) throws IOException {
        SVDFeatureInstance ins = dao.getNextInstance();
        if (ins == null) {
            return null;
        }
        double output = 0;
        LearningOracle orc = new LearningOracle();

        orc.setInstanceLabel(ins.getLabel());
        ArrayList<Feature> gfeas = ins.getGlobalFeas();
        for (int i=0; i<gfeas.size(); i++) {
            int ind = gfeas.get(i).getIndex();
            output += gbiases.getEntry(ind, 0) * gfeas.get(i).getValue();
            orc.addVarIndex(ind);
            orc.addVariable(gbiases.getEntry(ind, 0));
            orc.addGradient(gfeas.get(i).getValue());
        }

        ArrayList<Feature> ufeas = ins.getUserFeas();
        double[] ufactSum = new double[factDim];
        ArrayHelper.initialize(ufactSum, 0);
        for (int i=0; i<ufeas.size(); i++) {
            int index = ufeas.get(i).getIndex();
            int ind = getUserFeaFactIndex(index);
            double[] feaFact = ufacts.getRow(index);
            if (k == 0) {
                for (int j=0; j<factDim; j++) {
                    orc.addVarIndex(ind + j);
                    orc.addVariable(feaFact[j]);
                }
            }
            ArrayHelper.scale(feaFact, ufeas.get(i).getValue());
            ArrayHelper.addition(ufactSum, feaFact);
        }

        ArrayList<Feature> ifeas = ins.getItemFeas();
        double[] ifactSum = new double[factDim];
        ArrayHelper.initialize(ifactSum, 0);
        for (int i=0; i<ifeas.size(); i++) {
            int index = ifeas.get(i).getIndex();
            int ind = getItemFeaFactIndex(index);
            double[] feaFact = ifacts.getRow(index);
            if (k == 1) {
                for (int j=0; j<factDim; j++) {
                    orc.addVarIndex(ind + j);
                    orc.addVariable(feaFact[j]);
                }
            }
            ArrayHelper.scale(feaFact, ifeas.get(i).getValue());
            ArrayHelper.addition(ifactSum, feaFact);
        }
        
        if (k == 0) {
            double[] kernelGrad = kernel.getGradient(ufactSum, ifactSum, true);
            for (int i=0; i<ufeas.size(); i++) {
                for (int j=0; j<factDim; j++) {
                    orc.addGradient(kernelGrad[j] * ufeas.get(i).getValue());
                }
            }
        }

        if (k == 1) {
            double[] kernelGrad = kernel.getGradient(ufactSum, ifactSum, false);
            for (int i=0; i<ifeas.size(); i++) {
                for (int j=0; j<factDim; j++) {
                    orc.addGradient(kernelGrad[j] * ifeas.get(i).getValue());
                }
            }
        }

        output += kernel.getValue(ufactSum, ifactSum);
        orc.setModelOutput(output);
        orc.setInstanceLabel(ins.getLabel());
        return orc;
    }

    public void startNewIteration() throws IOException {
        dao.goBackToBeginning();
    }

    public double getVariable(int ind) {
        if (ind < numGlobalFeas) {
            return gbiases.getEntry(ind, 0);
        } else if (ind < numGlobalFeas + numUserFeas * factDim) {
            int rowInd = (ind - numGlobalFeas) / factDim;
            int colInd = (ind - numGlobalFeas) % factDim;
            return ufacts.getEntry(rowInd, colInd);
        } else {
            int rowInd = (ind - numGlobalFeas - numUserFeas * factDim) / factDim;
            int colInd = (ind - numGlobalFeas - numUserFeas * factDim) % factDim;
            return ifacts.getEntry(rowInd, colInd);
        }
    }

    public void setVariable(int ind, double var) {
        if (ind < numGlobalFeas) {
            gbiases.setEntry(ind, 0, var);
        } else if (ind < numGlobalFeas + numUserFeas * factDim) {
            int rowInd = (ind - numGlobalFeas) / factDim;
            int colInd = (ind - numGlobalFeas) % factDim;
            ufacts.setEntry(rowInd, colInd, var);
        } else {
            int rowInd = (ind - numGlobalFeas - numUserFeas * factDim) / factDim;
            int colInd = (ind - numGlobalFeas - numUserFeas * factDim) % factDim;
            ifacts.setEntry(rowInd, colInd, var);
        }
    }

    private int getUserFeaFactIndex(int index) { // map user feature factor matrix to variable vector
        return numGlobalFeas + index * factDim;
    }

    private int getItemFeaFactIndex(int index) { // map item feature factor matrix to variable vector
        return numGlobalFeas + numUserFeas * factDim + index * factDim;
    }

    public int getFactDim() {
        return factDim;
    }

    public double predict(SVDFeatureInstance ins, boolean sigmoid) {
        double pred = 0;
        ArrayList<Feature> gfeas = ins.getGlobalFeas();
        for (int i=0; i<gfeas.size(); i++) {
            pred += gbiases.getEntry(gfeas.get(i).getIndex(), 0);
        }
        double[] ufactSum = new double[factDim];
        ArrayHelper.initialize(ufactSum, 0);
        ArrayList<Feature> ufeas = ins.getUserFeas();
        for (int i=0; i<ufeas.size(); i++) {
            ArrayHelper.addition(ufactSum, ufacts.getRow(ufeas.get(i).getIndex()));
        }
        double[] ifactSum = new double[factDim];
        ArrayHelper.initialize(ifactSum, 0);
        ArrayList<Feature> ifeas = ins.getItemFeas();
        for (int i=0; i<ifeas.size(); i++) {
            ArrayHelper.addition(ifactSum, ifacts.getRow(ifeas.get(i).getIndex()));
        }
        pred += kernel.getValue(ufactSum, ifactSum);
        if (sigmoid) {
            return 1 / (1 + Math.exp(-pred));
        } else {
            return pred;
        }
    }
}
