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
public class HmmSVDFeatureModel extends SVDFeatureModel {
    private int numPos;
    private RealVector start;
    private RealMatrix trans;

    public HmmSVDFeatureModel(int numPos, int numBiases, int numFactors, int factDim) {
    }

    public void assignVariables() {
        super.assignVariables();
        start = requestScalarVar("start", numPos, 0.0, true, true);
        trans = requestVectorVar("trans", numPos, numPos + 1, 0.0, true, true);
    }
}
