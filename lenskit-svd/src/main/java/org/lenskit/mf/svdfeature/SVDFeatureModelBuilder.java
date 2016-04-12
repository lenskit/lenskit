package org.lenskit.mf.svdfeature;

import org.lenskit.featurize.EntityDAO;
import org.lenskit.solver.LearningData;
import org.lenskit.solver.ObjectiveFunction;
import org.lenskit.solver.OptimizationMethod;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelBuilder implements Provider<SVDFeatureModel> {
    private SVDFeatureModel model;
    private OptimizationMethod method;
    private LearningData learningData;

    public SVDFeatureModelBuilder(int numBiases, int numFactors, int factDim,
                                  SVDFeatureInstanceDAO dao,
                                  ObjectiveFunction loss,
                                  OptimizationMethod method) {
        this.model = new SVDFeatureModel(numBiases, numFactors, factDim, loss);
        this.method = method;
        this.learningData = dao;
    }

    @Inject
    public SVDFeatureModelBuilder(EntityDAO dao, Set<String> biasFeas,
                                  Set<String> ufactFeas, Set<String> ifactFeas,
                                  ObjectiveFunction loss,
                                  OptimizationMethod method) {
        this.method = method;
        this.model = new SVDFeatureModel(biasFeas, ufactFeas, ifactFeas, loss);
        this.learningData = new SVDFeatureLearningData(dao, model);
    }

    public SVDFeatureModel get() {
        method.minimize(model, learningData);
        return model;
    }
}
