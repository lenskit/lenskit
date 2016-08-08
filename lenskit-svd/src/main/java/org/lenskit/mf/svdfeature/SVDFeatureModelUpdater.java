package org.lenskit.mf.svdfeature;

import org.lenskit.solver.LearningData;
import org.lenskit.solver.OnlineOptimizationMethod;

import javax.inject.Inject;

public class SVDFeatureModelUpdater {
    final private SVDFeatureModel model;
    final private OnlineOptimizationMethod method;

    @Inject
    public SVDFeatureModelUpdater(SVDFeatureModel model,
                                  OnlineOptimizationMethod method) {
        this.model = model;
        this.method = method;
    }

    public void update(LearningData learningData) {
        method.update(model, learningData);
    }
}
