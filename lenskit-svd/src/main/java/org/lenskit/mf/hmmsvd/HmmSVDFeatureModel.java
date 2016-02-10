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

    public void ebeMultiplyTo(RealVector base, RealVector other) {
        int len = base.getDimension();
        for (int i=0; i<len; i++) {
            base.setEntry(i, base.getEntry(i) * other.getEntry(i));
        }
    }

    public double stochastic_expectation(HmmSVDFeatureInstance ins) {
        //make sure ins.numPos == this.numPos
        //compute p(x|z)
        RealVector probs = MatrixUtils.createRealVector(new double[ins.numPos]);
        for (int i=0; i<ins.numPos; i++) {
            SVDFeatureInstance svdFeaIns = new SVDFeatureInstance(ins.gfeas, ins.ufeas, 
                    ins.pos2ifeas[i]);
            probs.setEntry(i, svdFea.predict(svdFeaIns, true));
        }
        //initialize alpha 0 and beta n-1
        RealVector alpha = MatrixUtils.createRealMatrix(ins.numObs, numPos);
        RealMatrix beta = MatrixUtils.createRealMatrix(ins.numObs, numPos);
        beta.getRowVector(numObs - 1).set(1.0);
        //compute alpha 0 to n-1 and beta n-2 to 0
        for (int i=0; i<n; i++) {
            RealVector alphai = alpha.getRowVector(i);
            act = ins.obs.get(i);
            if (act == numPos) {
                alphai.set(1.0);
                RealVector multi = start;
                if (i > 0) {
                    multi = trans.preMultiply(alpha.getRowVector(i-1));
                }
                ebeMultiplyTo(alphai.combineToSelf(1.0, -1.0, probs), multi);
                            
            } else {
                alphai.set(0.0);
                if (i == 0) {
                    alpha0.setEntry(act, probs.getEntry(act));
                } else {
                    alphai.setEntry(act, probs.getEntry(act) * alpha.getRowVector(i-1).dotProduct(
                            trans.getColumnVector(act)));
                }
            }
            if (i == 0) {
                continue;
            }
            int j = ins.numObs - 1 - i;
            RealVector betaj = beta.getRowVector(j);
            act = ins.obs.get(j+1);
            if (act == numPos) {
                betaj.set(1.0);
                ebeMultiplyTo(betaj.combineToSelf(1.0, -1.0, probs), beta.getRowVector(j+1));
                betaj.setSubVector(0, trans.operate(betaj));
            } else {
                betaj.set(0.0);
                betaj.combineToSelf(1.0, beta.getRowVector(j+1).getEntry(act) * probs.getEntry(act),
                        tran.getColumnVector(act));
            }
        }
        //compute gamma and xi
        gamma = MatrixUtils.createRealMatrix(ins.numObs, numPos);
        xi = new ArrayList<RealMatrix>(ins.numObs - 1);
        for (int i=0; i<ins.numObs; i++) {
            RealVector gammai = gamma.getRowVector(i);
            gammai.setSubVector(0, alpha.getRowVector(i).ebeMultiply(beta.getRowVector(i)));
            if (i > 0) {
                RealMatrix subXi = MatrixUtils.createRealMatrix(numPos, numPos);
                //XXX, to be continued
                xi.add(subXi);
            }
        }
        //fill in instances
        instances.clear();
    }

    public SVDFeatureModel stochastic_maximization() {
        //update start and trans with closed formula
        svdFea.setInstances(instances);
        return svdFea;
    }
}
