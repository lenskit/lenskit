package org.lenskit.mf.svdfeature;

import org.lenskit.solver.OnlineOptimizationMethod;

public class SVDFeatureModelUpdater {
    private SVDFeatureModel model;
    private OnlineOptimizationMethod method;

    public SVDFeatureModelUpdater(SVDFeatureModel model,
                                  OnlineOptimizationMethod method) {
        this.model = model;
        this.method = method;
    }

    public void update(SVDFeatureLearningData learningData) {
        method.update(model, learningData);
    }
}
