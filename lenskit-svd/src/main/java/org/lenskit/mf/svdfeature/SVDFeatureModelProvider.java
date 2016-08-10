/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

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
 * Set up default configurations for {@link SVDFeatureModel} if not indicated and build a model with given learning data.
 *
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

    /**
     * @param entityType the EntityType to retrieve from learnDao or validDao.
     * @param learnDao the DAO used to construct learning data.
     * @param validDao the DAO used to construct validation data. Can be null, i.e. without validation.
     * @param featureExtractors the list of feature extractors to featurize. Can be null which will use the default
     *                              extractors including
                                    ConstantOneExtractor(SVDFeatureIndexName.BIASES.get(), "globalBias", "globalBiasIdx")
                                    LongToIdxExtractor(SVDFeatureIndexName.BIASES.get(), "user", "userBiasIdx")
                                    LongToIdxExtractor(SVDFeatureIndexName.BIASES.get(), "item", "itemBiasIdx")
                                    LongToIdxExtractor(SVDFeatureIndexName.FACTORS.get(), "user", "userFactIdx")
                                    LongToIdxExtractor(SVDFeatureIndexName.FACTORS.get(), "item", "itemFactIdx")
     * @param biasFeas the set of bias features. Can be null which will use the default
     *                 {"globalBiasIdx", "userBiasIdx", "itemBiasIdx"}
     * @param ufactFeas the set of user factor features. Can be null which will use the default {"userFactIdx"}
     * @param ifactFeas the set of item factor features. Can be null which will use the default {"itemFactIdx"}
     * @param biasSize the initial size of the scalar variable space with name SVDFeatureIndexName.BIASES.get()
     * @param factSize the initial size of the vector variable space with name SVDFeatureIndexName.FACTORS.get()
     * @param factDim the dimension of the factorization.
     * @param labelName the label attribute name used for featurization and prediction.
     * @param weightName the label attribute name used for featurization and optimization.
     * @param loss the objective function for the problem, i.e. LogisticLoss for binary classification and L2NormLoss
     *             for regression.
     * @param method the online optimization method used to learn the svdfeature model.
     */
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
