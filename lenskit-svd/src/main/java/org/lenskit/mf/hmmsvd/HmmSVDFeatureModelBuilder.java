package org.lenskit.mf.hmmsvd;

import java.io.IOException;

import org.lenskit.solver.method.ExpectationMaximization;
import org.lenskit.solver.method.OptimizationMethod;
import org.lenskit.solver.objective.LogisticLoss;
import org.lenskit.solver.objective.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureModelBuilder {
    private HmmSVDFeatureModel model;
    private OptimizationMethod method;
    private ObjectiveFunction loss;

    public HmmSVDFeatureModelBuilder(int numPos, int numBiases, int numFactors, int factDim,
                                     HmmSVDFeatureInstanceDAO dao, String svdFeaFile) {
        try {
            model = new HmmSVDFeatureModel(numPos, numBiases, numFactors, factDim, dao, svdFeaFile);
            method = new ExpectationMaximization();
            loss = new LogisticLoss();
        } catch (IOException e) {}
    }

    public HmmSVDFeatureModel build() throws IOException {
        model.assignVariables();
        method.minimize(model, loss);
        return model;
    }
}
