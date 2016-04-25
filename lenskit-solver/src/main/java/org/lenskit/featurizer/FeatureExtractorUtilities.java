package org.lenskit.featurizer;

import org.lenskit.space.IndexSpace;

import java.util.List;

public class FeatureExtractorUtilities {
    private FeatureExtractorUtilities() {}

    static public void getOrSetIndexSpaceToFeaturize(List<Feature> features,
                                                     boolean update,
                                                     IndexSpace indexSpace,
                                                     String indexName, Object key) {
        if (indexSpace.containsKey(indexName, key)) {
            Feature feature = new Feature(indexSpace.getIndexForKey(indexName, key), 1.0);
            features.add(feature);
        } else if (update) {
            int index = indexSpace.setKey(indexName, key);
            Feature feature = new Feature(index, 1.0);
            features.add(feature);
        }
    }
}
