package org.lenskit.mf.svdfeature;

import org.lenskit.featurize.EntityDAO;
import org.lenskit.featurize.FeatureExtractor;
import org.lenskit.solver.LearningData;
import org.lenskit.solver.ObjectiveFunction;
import org.lenskit.solver.OptimizationMethod;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelBuilder implements Provider<SVDFeatureModel> {
    final private SVDFeatureModel model;
    final private OptimizationMethod method;
    final private LearningData learningData;

    public SVDFeatureModelBuilder(int biasSize, int factSize, int factDim,
                                  SVDFeatureInstanceDAO dao,
                                  ObjectiveFunction loss,
                                  OptimizationMethod method) {
        this.model = new SVDFeatureModel(biasSize, factSize, factDim, loss);
        this.method = method;
        this.learningData = dao;
    }

    //biasFeas, ufactFeas and ifactFeas should be from the configuration
    //especially, label and weight are hard-coded attribute name for now
    @Inject
    public SVDFeatureModelBuilder(EntityDAO dao,
                                  List<FeatureExtractor> featureExtractors,
                                  Set<String> biasFeas,
                                  Set<String> ufactFeas,
                                  Set<String> ifactFeas,
                                  int biasSize,
                                  int factSize,
                                  int factDim,
                                  String labelName, String weightName,
                                  ObjectiveFunction loss,
                                  OptimizationMethod method) {
        this.method = method;
        this.model = new SVDFeatureModel(biasFeas, ufactFeas, ifactFeas, labelName, weightName,
                                         featureExtractors, biasSize, factSize, factDim, loss);
        this.learningData = new SVDFeatureEntityDAO(dao, model);
    }

    public SVDFeatureModel get() {
        method.minimize(model, learningData);
        return model;
    }
}
