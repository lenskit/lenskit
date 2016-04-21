package org.lenskit.featurize;

import org.lenskit.space.IndexSpace;
import org.lenskit.space.VariableSpace;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface FeatureExtractor extends Serializable {
    Map<String, List<Feature>> extract(Entity entity, boolean update,
                                       IndexSpace indexSpace);
}
