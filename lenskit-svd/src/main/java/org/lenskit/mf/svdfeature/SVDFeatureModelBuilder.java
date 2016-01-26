package org.grouplens.lenskit.mf.svdfeature;

import java.io.IOException;

import org.grouplens.lenskit.solver.objective.ObjectiveFunction;
import org.grouplens.lenskit.solver.method.OptimizationMethod;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelBuilder {
    private double tol;
    private double l1coef;
    private double l2coef;
    private double learningRate;
    private int maxIter;
    private SVDFeatureModel model;
    private ObjectiveFunction loss;
    private OptimizationMethod method;

    public SVDFeatureModelBuilder(int numGlobalFeas, int numUserfeas, int numItemFeas, int factDim,
                                  SVDFeatureInstanceDAO dao, KernelFunction inKernel, 
                                  ObjectiveFunction inLoss, OptimizationMethod inMethod, 
                                  double inL1coef, double inL2coef, int inMaxIter, 
                                  double inLearningRate, double inTol) {
        model = new SVDFeatureModel(numGlobalFeas, numUserfeas, numItemFeas, factDim, dao, inKernel);
        loss = inLoss;
        method = inMethod;
        tol = inTol;
        l1coef = inL1coef;
        l2coef = inL2coef;
        learningRate = inLearningRate;
        maxIter = inMaxIter;
    }

    public SVDFeatureModel get() throws IOException {
        method.minimize(model, loss, tol, maxIter, l1coef, l2coef, learningRate);
        return model;
    }
}
