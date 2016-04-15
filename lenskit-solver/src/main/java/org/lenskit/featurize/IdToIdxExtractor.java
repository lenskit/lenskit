package org.lenskit.featurize;

import org.lenskit.space.SynchronizedIndexSpace;

import java.util.ArrayList;
import java.util.List;

public class IdToIdxExtractor implements FeatureExtractor {
    private final SynchronizedIndexSpace indexSpace;
    private final String indexName;
    private final boolean update;
    private final String attrName;

    public IdToIdxExtractor(SynchronizedIndexSpace indexSpace, String indexName, boolean update,
                            String attrName) {
        this.indexName = indexName;
        this.indexSpace = indexSpace;
        this.update = update;
        this.attrName = attrName;
    }

    public List<Feature> extract(Entity entity) {
        List<Feature> features = new ArrayList<>();
        List<String> attrs = entity.getCatAttr(attrName);
        for (String attr : attrs) {
            if (indexSpace.containsStringKey(indexName, attr)) {
                Feature feature = new Feature(indexSpace.getIndexForStringKey(indexName, attr), 1.0);
                features.add(feature);
            } else if (update) {
                int index = indexSpace.setStringKey(indexName, attr);
                Feature feature = new Feature(index, 1.);
                features.add(feature);
            }
        }
        return features;
    }
}
