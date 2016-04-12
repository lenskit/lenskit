package org.lenskit.mf.svdfeature;

import org.lenskit.featurize.Entity;
import org.lenskit.featurize.EntityDAO;
import org.lenskit.solver.LearningData;
import org.lenskit.solver.LearningInstance;

public class SVDFeatureLearningData implements LearningData {

    private final EntityDAO entityDAO;
    private final SVDFeatureModel model;

    public SVDFeatureLearningData(EntityDAO entityDAO,
                                  SVDFeatureModel model) {
        this.entityDAO = entityDAO;
        this.model = model;
    }

    public LearningInstance getLearningInstance() {
        Entity entity = entityDAO.getNextEntity();
        if (entity == null) {
            return null;
        } else {
            return model.featurize(entity, true);
        }
    }

    public void startNewIteration() {
        entityDAO.restart();
    }
}
