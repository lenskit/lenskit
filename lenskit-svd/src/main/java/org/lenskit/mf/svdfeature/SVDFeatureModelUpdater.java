package org.lenskit.mf.svdfeature;

import org.lenskit.solver.OnlineOptimizationMethod;

public class SVDFeatureModelUpdater {
    final private SVDFeatureModel model;
    final private OnlineOptimizationMethod method;

    public SVDFeatureModelUpdater(SVDFeatureModel model,
                                  OnlineOptimizationMethod method) {
        this.model = model;
        this.method = method;
    }

    public void update(LearningData learningData) {
        method.update(model, learningData);
    }
}
