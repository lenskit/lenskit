package org.lenskit.featurizer;

import org.lenskit.solver.LearningInstance;

public interface Featurizer {
    LearningInstance featurize(Entity entity, boolean update);
}
