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
    private int numPos;
    private RealVector start;
    private RealMatrix trans;
    private SVDFeatureModel svdFea;
    private ArrayList<SVDFeatureInstance> instances;
    private HmmSVDFeatureInstanceDAO dao;

    public HmmSVDFeatureModel(int inNumPos, int numBiases, int numFactors, int factDim, 
                              HmmSVDFeatureInstanceDAO inDao) {
        svdFea = new SVDFeatureModel(numBiases, numFactors, factDim);
        dao = inDao;
        numPos = inNumPos;
    }

    public void assignVariables() {
        svdFea.assignVariables();
        start = requestScalarVar("start", numPos, 0.0, true, true);
        trans = requestVectorVar("trans", numPos, numPos + 1, 0.0, true, true);
    }

    public double expectation(HmmSVDFeatureInstance ins) {
        //fill in instances
    }

    public SVDFeatureModel maximization() {
        //update start and trans
        svdFea.setInstances(instances);
        return svdFea;
    }
}
