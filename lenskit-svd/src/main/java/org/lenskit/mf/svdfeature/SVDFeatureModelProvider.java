package org.lenskit.mf.svdfeature;

import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.EntityType;
import org.lenskit.featurizer.ConstantOneExtractor;
import org.lenskit.featurizer.FeatureExtractor;
import org.lenskit.featurizer.LongToIdxExtractor;
import org.lenskit.mf.funksvd.FeatureCount;
import org.lenskit.solver.LearningData;
import org.lenskit.solver.ObjectiveFunction;
import org.lenskit.solver.OptimizationMethod;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelProvider implements Provider<SVDFeatureModel> {
    final private SVDFeatureModel model;
    final private OptimizationMethod method;
    final private LearningData learningData;
    final private LearningData validData;

    private List<FeatureExtractor> getDefaultFeatureExtractors() {
        List<FeatureExtractor> featureExtractors = new ArrayList<>();
        featureExtractors.add(new ConstantOneExtractor(SVDFeatureIndexName.BIASES.get(),
                                                       "globalBias", "globalBiasIdx"));
        featureExtractors.add(new LongToIdxExtractor(SVDFeatureIndexName.BIASES.get(),
                                                     "user", "userBiasIdx"));
        featureExtractors.add(new LongToIdxExtractor(SVDFeatureIndexName.BIASES.get(),
                                                     "item", "itemBiasIdx"));
        featureExtractors.add(new LongToIdxExtractor(SVDFeatureIndexName.FACTORS.get(),
                                                     "user", "userFactIdx"));
        featureExtractors.add(new LongToIdxExtractor(SVDFeatureIndexName.FACTORS.get(),
                                                     "item", "itemFactIdx"));
        return featureExtractors;
    }

    private Set<String> getDefaultBiasFeatures() {
        String[] bFeas = {"globalBiasIdx", "userBiasIdx", "itemBiasIdx"};
        return new HashSet<>(Arrays.asList(bFeas));
    }

    private Set<String> getDefaultUserFactorFeatures() {
        String[] uFeas = {"userFactIdx"};
        return new HashSet<>(Arrays.asList(uFeas));
    }

    private Set<String> getDefaultItemFactorFeatures() {
        String[] iFeas = {"itemFactIdx"};
        return new HashSet<>(Arrays.asList(iFeas));
    }

    private EntityType getDefaultEntityType() {
        return CommonTypes.RATING;
    }

    @Inject
    public SVDFeatureModelProvider(@Nullable @SVDFeatureEntityType EntityType entityType,
                                   DataAccessObject learnDao,
                                   DataAccessObject validDao,
                                   @Nullable @FeatureExtractors List<FeatureExtractor> featureExtractors,
                                   @Nullable @BiasFeatures Set<String> biasFeas,
                                   @Nullable @UserFactorFeatures Set<String> ufactFeas,
                                   @Nullable @ItemFactorFeatures Set<String> ifactFeas,
                                   @InitialBiasFactorSize int biasSize,
                                   @InitialBiasFactorSize int factSize,
                                   @FeatureCount int factDim,
                                   @DefaultLabelName String labelName,
                                   @DefaultWeightName String weightName,
                                   ObjectiveFunction loss,
                                   OptimizationMethod method) {
        if (featureExtractors == null) {
            featureExtractors = getDefaultFeatureExtractors();
        }
        if (biasFeas == null) {
            biasFeas = getDefaultBiasFeatures();
        }
        if (ufactFeas == null) {
            ufactFeas = getDefaultUserFactorFeatures();
        }
        if (ifactFeas == null) {
            ifactFeas = getDefaultItemFactorFeatures();
        }
        if (entityType == null) {
            entityType = getDefaultEntityType();
        }
        this.method = method;
        this.model = new SVDFeatureModel(biasFeas, ufactFeas, ifactFeas, labelName, weightName,
                                         featureExtractors, biasSize, factSize, factDim, loss);
        this.learningData = new SVDFeatureLearningData(entityType, learnDao, model);
        if (validDao == null || learnDao.equals(validDao)) {
            this.validData = null;
        } else {
            this.validData = new SVDFeatureLearningData(entityType, validDao, model);
        }
    }

    public SVDFeatureModel get() {
        method.minimize(model, learningData, validData);
        return model;
    }
}
