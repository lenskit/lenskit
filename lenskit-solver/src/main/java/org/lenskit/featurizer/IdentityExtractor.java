package org.lenskit.featurizer;

import org.lenskit.space.IndexSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentityExtractor implements FeatureExtractor {
    private final String indexName;
    private final String attrName;
    private final String feaName;

    public IdentityExtractor(String indexName, String attrName, String feaName) {
        this.attrName = attrName;
        this.feaName = feaName;
        this.indexName = indexName;
    }

    public Map<String, List<Feature>> extract(Entity entity, boolean update,
                                              IndexSpace indexSpace) {
        List<Feature> feaList = new ArrayList<>();
        if (entity.hasNumAttr(attrName)) {
            double val = entity.getNumAttr(attrName);
            if (indexSpace.containsKey(indexName, attrName)) {
                Feature feature = new Feature(
                        indexSpace.getIndexForKey(indexName, attrName), val);
                feaList.add(feature);
            } else if (update) {
                int index = indexSpace.setKey(indexName, attrName);
                Feature feature = new Feature(index, val);
                feaList.add(feature);
            }
        }
        Map<String, List<Feature>> feaMap = new HashMap<>();
        feaMap.put(feaName, feaList);
        return feaMap;
    }
}