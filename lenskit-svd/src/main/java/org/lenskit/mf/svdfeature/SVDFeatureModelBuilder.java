package org.lenskit.mf.svdfeature;

import org.lenskit.solver.StochasticGradientDescent;
import org.lenskit.solver.ObjectiveFunction;
import org.lenskit.solver.OptimizationMethod;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelBuilder implements Provider<SVDFeatureModel> {
    private SVDFeatureModel model;
    private ObjectiveFunction loss;
    private OptimizationMethod method;

    @Inject
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

    public SVDFeatureModel get() {
        model.assignVariables();
        method.minimize(model, loss);
        return model;
    }
}
