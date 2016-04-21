package org.lenskit.featurize;

import org.lenskit.space.IndexSpace;
import org.lenskit.space.SynchronizedIndexSpace;
import org.lenskit.space.VariableSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdToIdxExtractor implements FeatureExtractor {
    private final String indexName;
    private final String attrName;
    private final String feaName;

    public IdToIdxExtractor(String indexName,
                            String attrName,
                            String feaName) {
        this.indexName = indexName;
        this.attrName = attrName;
        this.feaName = feaName;
    }

    public Map<String, List<Feature>> extract(Entity entity, boolean update,
                                              IndexSpace indexSpace) {
        List<Feature> features = new ArrayList<>();
        List<String> attrs = entity.getCatAttr(attrName);
        for (String attr : attrs) {
            String key = attrName + ":" + attr;
            if (indexSpace.containsStringKey(indexName, key)) {
                Feature feature = new Feature(indexSpace.getIndexForStringKey(indexName, key), 1.0);
                features.add(feature);
            } else if (update) {
                int index = indexSpace.setStringKey(indexName, key);
                Feature feature = new Feature(index, 1.0);
                features.add(feature);
            }
        }
        Map<String, List<Feature>> feaMap = new HashMap<>();
        feaMap.put(feaName, features);
        return feaMap;
    }
}
