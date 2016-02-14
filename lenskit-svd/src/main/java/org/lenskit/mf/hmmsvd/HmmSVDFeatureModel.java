package org.lenskit.mf.hmmsvd;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.function.Log;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.DoubleArray;
import org.lenskit.mf.svdfeature.SVDFeatureInstance;
import org.lenskit.mf.svdfeature.SVDFeatureModel;
import org.lenskit.solver.objective.LearningInstance;
import org.lenskit.solver.objective.LearningModel;
import org.lenskit.solver.objective.RandomInitializer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureModel extends LearningModel {
    private int numPos; //also represents no action
    private RealVector start;
    private RealMatrix trans;
    private SVDFeatureModel svdFea;
    private transient ArrayList<SVDFeatureInstance> instances;
    private transient HmmSVDFeatureInstanceDAO dao;
    private transient ArrayList<RealVector> gamma;
    private transient ArrayList<ArrayList<RealVector>> xi;
    private transient RealVector startUpdate;
    private transient ArrayList<RealVector> transUpdate;

    public HmmSVDFeatureModel(int inNumPos, int numBiases, int numFactors, int factDim, 
                              HmmSVDFeatureInstanceDAO inDao) {
        svdFea = new SVDFeatureModel(numBiases, numFactors, factDim);
        instances = new ArrayList<>();
        dao = inDao;
        numPos = inNumPos;
        startUpdate = MatrixUtils.createRealVector(new double[numPos]);
        transUpdate = new ArrayList<>(numPos);
        for (int i=0; i<numPos; i++) {
            transUpdate.add(MatrixUtils.createRealVector(new double[numPos]));
        }
    }

    public void assignVariables() {
        svdFea.assignVariables();

        RandomInitializer randInit = new RandomInitializer();
        start = MatrixUtils.createRealVector(new double[numPos]);
        randInit.randInitVector(start, true);
        trans = MatrixUtils.createRealMatrix(numPos, numPos);
        randInit.randInitMatrix(trans, true);
    }

    public void stochasticInference(HmmSVDFeatureInstance ins, ArrayList<RealVector> outGamma,
            ArrayList<ArrayList<RealVector>> outXi) {
        //compute p(x|z)
        RealVector probs = MatrixUtils.createRealVector(new double[ins.numPos]);
        for (int i=0; i<ins.numPos; i++) {
            SVDFeatureInstance svdFeaIns = new SVDFeatureInstance(ins.pos2gfeas.get(i), ins.ufeas,
                    ins.pos2ifeas.get(i));
            probs.setEntry(i, svdFea.predict(svdFeaIns, true));
        }
        ArrayList<RealVector> probX = new ArrayList<>(ins.numObs);
        for (int i=0; i<ins.numObs; i++) {
            int act = ins.obs.get(i);
            RealVector probi = MatrixUtils.createRealVector(new double[numPos]);
            if (act == numPos) {
                probi.set(1.0);
                probi.combineToSelf(1.0, -1.0, probs);
            } else {
                probi.set(0.0);
                probi.setEntry(act, probs.getEntry(act));
            }
            probX.add(probi);
        }
        //initialize alpha and beta n-1
        ArrayList<RealVector> alphaHat = new ArrayList<>(ins.numObs);
        ArrayList<RealVector> betaHat = new ArrayList<>(ins.numObs);
        RealVector c = MatrixUtils.createRealVector(new double[ins.numObs]);
        //compute alphaHat 0 to n-1
        for (int i=0; i<ins.numObs; i++) {
            RealVector probx = probX.get(i);
            RealVector alpha = null;
            if (i == 0) {
                alpha = probx.ebeMultiply(start);
            } else if (i > 0) {
                alpha = probx.ebeMultiply(trans.preMultiply(alphaHat.get(i - 1)));
            }
            c.setEntry(i, StatUtils.sum(((ArrayRealVector)alpha).getDataRef()));
            alpha.mapDivideToSelf(c.getEntry(i));
            alphaHat.add(alpha);
            betaHat.add(MatrixUtils.createRealVector(new double[numPos]));
        }
        //compute betaHat n-1 to 0
        betaHat.get(ins.numObs - 1).set(1.0);
        for (int j=ins.numObs - 2; j>=0; j--) {
            RealVector betaj = betaHat.get(j);
            RealVector probx = probX.get(j + 1);
            double cj = c.getEntry(j + 1);
            betaj.setSubVector(0, trans.operate(probx.ebeMultiply(betaHat.get(j + 1))).mapDivideToSelf(cj));
        }
        if (Double.isNaN(alphaHat.get(ins.numObs - 1).getEntry(0)) || Double.isNaN(betaHat.get(0).getEntry(0))) {
            int x = 1;
        }
        //compute gamma and xi
        for (int i=0; i<ins.numObs; i++) {
            outGamma.add(alphaHat.get(i).ebeMultiply(betaHat.get(i)));
            if (i > 0) {
                RealVector probx = probX.get(i);
                RealVector betai = betaHat.get(i);
                RealVector alphai = alphaHat.get(i - 1);
                double ci = c.getEntry(i);
                ArrayList<RealVector> subXi = new ArrayList<>(numPos);
                for (int j=0; j<numPos; j++) {
                    subXi.add(probx.ebeMultiply(trans.getRowVector(j))
                            .ebeMultiply(betai).mapMultiplyToSelf(alphai.getEntry(j) * ci));
                }
                outXi.add(subXi);
            }
        }
    }

    public double stochasticExpectation(LearningInstance inIns) {
        HmmSVDFeatureInstance ins;
        if (inIns instanceof HmmSVDFeatureInstance) {
            ins = (HmmSVDFeatureInstance)inIns;
        } else {
            return 0.0;
        }
        //make sure ins.numPos == this.numPos
        gamma = new ArrayList<>(ins.numObs);
        xi = new ArrayList<>(ins.numObs - 1);
        stochasticInference(ins, gamma, xi);
        //fill in instances
        instances.clear();
        for (int i=0; i<ins.numObs; i++) {
            int act = ins.obs.get(i);
            for (int j=0; j<numPos; j++) {
                double weight = gamma.get(i).getEntry(j);
                if (weight != 0.0 && (j == act || act == numPos)) {
                    SVDFeatureInstance svdFeaIns = new SVDFeatureInstance(ins.pos2gfeas.get(j), ins.ufeas,
                                                                          ins.pos2ifeas.get(j));
                    svdFeaIns.weight = weight;
                    if (j == act) {
                        svdFeaIns.label = 1.0;
                    } else {
                        svdFeaIns.label = 0.0;
                    }
                    instances.add(svdFeaIns);
                }
            }
        }
        //closed form stochastic maximization: update startUpdate and transUpdate with closed formula
        RealVector gamma0 = gamma.get(0);
        startUpdate.combineToSelf(1.0, 1.0, gamma0);
        for (int i=0; i<numPos; i++) {
            for (int j=0; j<ins.numObs-1; j++) {
                ArrayList<RealVector> cxi = xi.get(j);
                transUpdate.get(i).combineToSelf(1.0, 1.0, cxi.get(i));
            }
        }
        if (Double.isNaN(startUpdate.getEntry(0)) || Double.isNaN(transUpdate.get(0).getEntry(0))
                || Double.isNaN(transUpdate.get(ins.numPos - 1).getEntry(0))) {
            int x = 1;
        }
        //compute the objective value of the closed form part
        UnivariateFunction log = new Log();
        double startObjVal = gamma0.dotProduct(start.map(log));
        double transObjVal = 0.0;
        for (int i=0; i<ins.numObs-1; i++) {
            ArrayList<RealVector> cxi = xi.get(i);
            for (int j=0; j<numPos; j++) {
                transObjVal += cxi.get(j).dotProduct(trans.getRowVector(j).mapToSelf(log));
            }
        }
        return -(startObjVal + transObjVal);
    }

    public SVDFeatureModel stochasticMaximization() {
        svdFea.setInstances(instances);
        return svdFea;
    }

    public LearningModel maximization() { //closed form maximization
        start.setSubVector(0, startUpdate);
        double sum = StatUtils.sum(((ArrayRealVector)start).getDataRef());
        start.mapDivideToSelf(sum);
        for (int i=0; i<numPos; i++) {
            RealVector trUp = transUpdate.get(i);
            sum = StatUtils.sum(((ArrayRealVector)trUp).getDataRef());
            trans.setRowVector(i, trUp.mapDivideToSelf(sum));
        }
        return null;
    }

    public SVDFeatureModel getSVDFeatureModel() {
        return svdFea;
    }

    public HmmSVDFeatureInstance getLearningInstance() {
        HmmSVDFeatureInstance ins;
        try {
            do {
                ins = dao.getNextInstance();
                if (ins == null) {
                    return null;
                }
            } while (ins.numObs > 120 || ins.numObs < 2) ;
        } catch (IOException e) {
            ins = null;
        }
        return ins;
    }

    public void startNewIteration() {
        startUpdate.set(0.0);
        for (int i=0; i<numPos; i++) {
            transUpdate.get(i).set(0.0);
        }
        try {
            dao.goBackToBeginning();
        } catch (IOException e) { }
    }
}
