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
    private OptimizationMethod method;
    private SVDFeatureInstanceDAO dao;

    public SVDFeatureModelBuilder(int numBiases, int numFactors, int factDim,
                                  SVDFeatureInstanceDAO dao, ObjectiveFunction loss) {
        this.model = new SVDFeatureModel(numBiases, numFactors, factDim, loss);
        this.method = new StochasticGradientDescent();
        this.dao = dao;
    }

    @Inject
    public SVDFeatureModelBuilder(SVDFeatureRawDAO dao, ObjectiveFunction loss) {
        this.model = new SVDFeatureModel(dao, loss);
        this.method = new StochasticGradientDescent();
        // get instance dao
    }

    public SVDFeatureModel get() {
        method.minimize(model, dao);
        return model;
    }
}
