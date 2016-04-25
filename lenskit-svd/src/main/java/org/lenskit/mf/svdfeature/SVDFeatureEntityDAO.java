package org.lenskit.mf.svdfeature;

import org.lenskit.featurizer.Entity;
import org.lenskit.featurizer.EntityDAO;
import org.lenskit.solver.LearningData;
import org.lenskit.solver.LearningInstance;

public class SVDFeatureEntityDAO implements LearningData {

    private final EntityDAO entityDAO;
    private final SVDFeatureModel model;

    public SVDFeatureEntityDAO(EntityDAO entityDAO,
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
