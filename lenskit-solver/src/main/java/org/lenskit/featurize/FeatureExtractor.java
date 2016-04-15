package org.lenskit.featurize;

import java.util.List;

public interface FeatureExtractor {
    List<Feature> extract(Entity entity);
}
