package org.lenskit.mf.svdfeature;

import org.lenskit.data.dao.file.EntitySource;
import org.lenskit.data.entities.Entity;
import org.lenskit.solver.LearningData;
import org.lenskit.solver.LearningInstance;
import org.lenskit.util.io.ObjectStream;

import java.io.IOException;

public class SVDFeatureEntityData implements LearningData {

    private final SVDFeatureModel model;
    private final EntitySource entitySource;
    private ObjectStream<Entity> entities = null;

    public SVDFeatureEntityData(EntitySource entitySource,
                                SVDFeatureModel model) {
        this.model = model;
        this.entitySource = entitySource;
    }

    public LearningInstance getLearningInstance() {
        if (entities == null) {
            return null;
        }
        Entity entity = entities.readObject();
        if (entity == null) {
            return null;
        } else {
            return model.featurize(entity, true);
        }
    }

    public void startNewIteration() {
        try {
            entities = entitySource.openStream();
        } catch (IOException e) {
            entities = null;
        }
    }
}
