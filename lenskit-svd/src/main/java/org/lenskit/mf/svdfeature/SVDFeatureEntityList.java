package org.lenskit.mf.svdfeature;

import org.lenskit.featurizer.Entity;
import org.lenskit.solver.LearningData;
import org.lenskit.solver.LearningInstance;

import java.util.List;

public class SVDFeatureEntityList implements LearningData {
    private int iter = 0;
    private final List<Entity> entityList;
    private final SVDFeatureModel model;

    SVDFeatureEntityList(SVDFeatureModel model, List<Entity> entityList) {
        this.entityList = entityList;
        this.model = model;
    }

    public LearningInstance getLearningInstance() {
        if (iter >= entityList.size()) {
            return null;
        }
        return model.featurize(entityList.get(iter++), true);
    }

    public void startNewIteration() {
        iter = 0;
    }
}
