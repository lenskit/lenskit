package org.lenskit.mf.svdfeature;

import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.featurizer.Featurizer;
import org.lenskit.solver.LearningData;
import org.lenskit.solver.LearningInstance;
import org.lenskit.util.io.ObjectStream;

public class SVDFeatureLearningData implements LearningData {

    private final Featurizer featurizer;
    private final DataAccessObject dao;
    private final EntityType entityType;
    private ObjectStream<Entity> entityStream = null;

    public SVDFeatureLearningData(EntityType entityType,
                                  DataAccessObject dao,
                                  Featurizer featurizer) {
        this.entityType = entityType;
        this.dao = dao;
        this.featurizer = featurizer;
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
            return featurizer.featurize(entity, true);
        }
    }

    public void startNewIteration() {
        entityStream = dao.streamEntities(entityType);
    }
}
