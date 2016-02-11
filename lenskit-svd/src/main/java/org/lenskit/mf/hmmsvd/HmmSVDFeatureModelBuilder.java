package org.lenskit.mf.hmmsvd;

import java.io.IOException;

import org.lenskit.solver.objective.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureModelBuilder {
    private HmmSVDFeatureModel model;
    private OptimizationMethod method;
    private ObjectiveFunction loss;

    public HmmSVDFeatureModelBuilder(int numPos, int numBiases, int numFactors, int factDim
                                     HmmSVDFeatureInstanceDAO dao) {
        model = new HmmSVDFeatureModel(numPos, numBiases, numFactors, factDim, dao);
        method = new StochasticExpectationMaximization();
        loss = new LogisticLoss();
    }

    public HmmSVDFeatureModel build() throws IOException {
        model.assignVariables();
        method.minimize(model, loss);
        return model;
    }
}
