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
    private ArrayList<SVDFeatureInstance> instances;
    private HmmSVDFeatureInstanceDAO dao;
    private ArrayList<RealVector> gamma;
    private ArrayList<ArrayList<RealVector>> xi;
    private RealVector startUpdate;
    private ArrayList<RealVector> transUpdate;

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
        start = requestScalarVar("start", numPos, 0.0, true, true); //can initialize as trans by itself
        trans = MatrixUtils.createRealMatrix(numPos, numPos);
        RandomInitializer randInit = new RandomInitializer();
        randInit.randInitMatrix(trans, true);
    }

    public double stochastic_expectation(LearningInstance inIns) {
        HmmSVDFeatureInstance ins;
        if (inIns instanceof HmmSVDFeatureInstance) {
            ins = (HmmSVDFeatureInstance)inIns;
        } else {
            return 0.0;
        }
        //make sure ins.numPos == this.numPos
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
        ArrayList<RealVector> alpha = new ArrayList<>(ins.numObs);
        ArrayList<RealVector> beta = new ArrayList<>(ins.numObs);
        for (int i=0; i<ins.numObs; i++) {
            beta.add(MatrixUtils.createRealVector(new double[numPos]));
        }
        beta.get(ins.numObs - 1).set(1.0);
        //compute alpha 0 to n-1 and beta n-2 to 0
        for (int i=0; i<ins.numObs; i++) {
            RealVector probx = probX.get(i);
            if (i == 0) {
                alpha.add(probx.ebeMultiply(start));
                continue;
            } else if (i > 0) {
                alpha.add(probx.ebeMultiply(trans.preMultiply(alpha.get(i - 1))));
            }
            int j = ins.numObs - 1 - i;
            RealVector betaj = beta.get(j);
            probx = probX.get(j + 1);
            betaj.setSubVector(0, trans.operate(probx.ebeMultiply(beta.get(j + 1))));
        }
        double pX = StatUtils.sum(((ArrayRealVector)(alpha.get(ins.numObs - 1))).getDataRef());
        if (pX == 0.0 || Double.isNaN(pX) || Double.isNaN(beta.get(0).getEntry(0))) {
            int x = 1;
        }
        //compute gamma and xi
        gamma = new ArrayList<>(ins.numObs); //, numPos);
        xi = new ArrayList<>(ins.numObs - 1);
        for (int i=0; i<ins.numObs; i++) {
            gamma.add(alpha.get(i).ebeMultiply(beta.get(i)).mapDivideToSelf(pX));
            if (i > 0) {
                RealVector probx = probX.get(i);
                RealVector betai = beta.get(i);
                RealVector alphai = alpha.get(i - 1);
                ArrayList<RealVector> subXi = new ArrayList<>(numPos);
                for (int j=0; j<numPos; j++) {
                    subXi.add(probx.ebeMultiply(trans.getRowVector(j))
                            .ebeMultiply(betai).mapMultiplyToSelf(alphai.getEntry(j) / pX));
                }
                xi.add(subXi);
            }
        }
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

    public SVDFeatureModel stochastic_maximization() {
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
