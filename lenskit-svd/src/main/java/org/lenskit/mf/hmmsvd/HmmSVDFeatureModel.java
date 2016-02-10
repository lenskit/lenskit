package org.lenskit.mf.hmmsvd;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.mf.svdfeature.SVDFeatureModel;
import org.lenskit.solver.objective.LatentVariableModel;
import org.lenskit.solver.objective.StochasticOracle;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureModel extends LatentVariableModel {
    private int numPos; //also represents no action
    private RealVector start;
    private RealMatrix trans;
    private SVDFeatureModel svdFea;
    private ArrayList<SVDFeatureInstance> instances;
    private HmmSVDFeatureInstanceDAO dao;
    private RealMatrix gamma;
    private ArrayList<RealMatrix> xi;

    public HmmSVDFeatureModel(int inNumPos, int numBiases, int numFactors, int factDim, 
                              HmmSVDFeatureInstanceDAO inDao) {
        svdFea = new SVDFeatureModel(numBiases, numFactors, factDim);
        instances = new ArrayList<SVDFeatureInstance>();
        dao = inDao;
        numPos = inNumPos;
    }

    public void assignVariables() {
        svdFea.assignVariables();
        start = requestScalarVar("start", numPos, 0.0, true, true);
        trans = requestVectorVar("trans", numPos, numPos + 1, 0.0, true, true);
    }

    public double stochastic_expectation(HmmSVDFeatureInstance ins) {
        //make sure ins.numPos == this.numPos
        //compute p(x|z)
        RealVector probs = MatrixUtils.createRealVector(new double[ins.numPos]);
        RealMatrix probX = MatrixUtils.createRealMatrix(ins.numObs, numPos);
        for (int i=0; i<ins.numPos; i++) {
            SVDFeatureInstance svdFeaIns = new SVDFeatureInstance(ins.gfeas, ins.ufeas, 
                    ins.pos2ifeas[i]);
            probs.setEntry(i, svdFea.predict(svdFeaIns, true));
        }
        for (int i=0; i<ins.numObs; i++) {
            int act = ins.obs.get(i);
            RealVector probi = probX.getRowVector(i);
            if (act == numPos) {
                probi.set(1.0);
                probi.combineToSelf(1.0, -1.0, probs);
            } else {
                probi.set(0.0);
                probi.setEntry(act, probs.getEntry(act));
            }
        }
        //initialize alpha and beta n-1
        RealVector alpha = MatrixUtils.createRealMatrix(ins.numObs, numPos);
        RealMatrix beta = MatrixUtils.createRealMatrix(ins.numObs, numPos);
        beta.getRowVector(numObs - 1).set(1.0);
        //compute alpha 0 to n-1 and beta n-2 to 0
        for (int i=0; i<ins.numObs; i++) {
            RealVector alphai = alpha.getRowVector(i);
            RealVector probx = probX.getRowVector(i);
            if (i == 0) {
                alphai.setSubVector(0, probx.ebeMultiply(start));
                continue;
            } else if (i > 0) {
                alphai.setSubVector(0, probx.ebeMultiply(trans.preMultiply(alpha.getRowVector(i - 1))));
            }
            int j = ins.numObs - 1 - i;
            RealVector betaj = beta.getRowVector(j);
            probx = probX.getRowVector(j + 1);
            betaj.setSubVector(0, trans.operate(probx.ebeMultiply(beta.getRowVector(j + 1))));
        }
        //compute gamma and xi
        gamma = MatrixUtils.createRealMatrix(ins.numObs, numPos);
        xi = new ArrayList<RealMatrix>(ins.numObs - 1);
        for (int i=0; i<ins.numObs; i++) {
            RealVector gammai = gamma.getRowVector(i);
            gammai.setSubVector(0, alpha.getRowVector(i).ebeMultiply(beta.getRowVector(i)));
            if (i > 0) {
                RealVector probx = probX.getRowVector(i);
                RealVector betai = beta.getRowVector(i);
                RealVector alphai = alpha.getRowVector(i - 1);
                RealMatrix subXi = MatrixUtils.createRealMatrix(numPos, numPos);
                for (int j=0; j<numPos; j++) {
                    subXi.getRowVector(j).setSubVector(0, probx.ebeMultiply(trans.getRowVector(j))
                            .ebeMultiply(betai).mapMultiplyToSelf(alphai.getEntry(j)));
                }
                xi.add(subXi);
            }
        }
    }

    public SVDFeatureModel stochastic_maximization() {
        //update start and trans with closed formula
        RealVector gamma0 = gamma.getRowVector(0);
        double sum = StatUtils.sum(((ArrayRealVector)gamma0).getDataRef());
        start.setSubVector(0, gamma0.mapDivide(sum));
        int numObs = xi.size();
        for (int i; i<numPos; i++) {
            RealVector transi = trans.getRowVector(i);
            transi.set(0.0);
            for (int j=0; j<numObs; j++) {

            }
        }

        instances.clear();
        //fill in instances
        svdFea.setInstances(instances);
        return svdFea;
    }
}
