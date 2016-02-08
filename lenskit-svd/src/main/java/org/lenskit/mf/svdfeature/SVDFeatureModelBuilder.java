package org.lenskit.mf.svdfeature;

import java.io.IOException;

import org.lenskit.solver.method.StochasticGradientDescent;
import org.lenskit.solver.objective.ObjectiveFunction;
import org.lenskit.solver.method.OptimizationMethod;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelBuilder {
    private SVDFeatureModel model;
    private ObjectiveFunction loss;
    private OptimizationMethod method;

    public SVDFeatureModelBuilder(int numBiases, int numFactors, int factDim,
                                  SVDFeatureInstanceDAO dao, ObjectiveFunction inLoss) {
        loss = inLoss;
        model = new SVDFeatureModel(numBiases, numFactors, factDim, dao);
        method = new StochasticGradientDescent();
    }

    public SVDFeatureModelBuilder(int numBiases, int numFactors, int factDim,
                                  SVDFeatureInstanceDAO dao, ObjectiveFunction inLoss, 
                                  double inL1coef, double inL2coef, int inMaxIter, 
                                  double inLearningRate, double inTol) {
        loss = inLoss;
        model = new SVDFeatureModel(numBiases, numFactors, factDim, dao);
        method = new StochasticGradientDescent(inMaxIter, inL2coef, inLearningRate, inTol);
    }

    public SVDFeatureModel build() {
        model.assignVariables();
        method.minimize(model, loss);
        return model;
    }
}
