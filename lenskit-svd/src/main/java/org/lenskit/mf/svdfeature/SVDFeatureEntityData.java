package org.lenskit.mf.svdfeature;

import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.solver.LearningData;
import org.lenskit.solver.LearningInstance;
import org.lenskit.util.io.ObjectStream;

public class SVDFeatureEntityData implements LearningData {

    private final SVDFeatureModel model;
    private final DataAccessObject dao;
    private final EntityType entityType;
    private ObjectStream<Entity> entityStream = null;

    public SVDFeatureEntityData(EntityType entityType, DataAccessObject dao,
                                SVDFeatureModel model) {
        this.model = model;
        this.entityType = entityType;
        this.dao = dao;
    }

    public LearningInstance getLearningInstance() {
        if (entityStream == null) {
            return null;
        }
        Entity entity = entityStream.readObject();
        if (entity == null) {
            entityStream.close();
            return null;
        } else {
            return model.featurize(entity, true);
        }
    }

    public void startNewIteration() {
        entityStream = dao.streamEntities(entityType);
    }
}
