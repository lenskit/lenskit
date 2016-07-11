package org.lenskit.mf.svdfeature;

import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.EntityType;
import org.lenskit.featurizer.FeatureExtractor;
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
    final private LearningData validData;

    //biasFeas, ufactFeas and ifactFeas should be from the configuration
    //especially, label and weight are hard-coded attribute name for now
    @Inject
    public SVDFeatureModelBuilder(EntityType entityType,
                                  DataAccessObject learnDao,
                                  DataAccessObject validDao,
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
        this.learningData = new SVDFeatureEntityData(entityType, learnDao, model);
        if (validDao != null) {
            this.validData = new SVDFeatureEntityData(entityType, validDao, model);
        } else {
            this.validData = null;
        }
    }

    public SVDFeatureModel get() {
        method.minimize(model, learningData, validData);
        return model;
    }
}
