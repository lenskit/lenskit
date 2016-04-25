package org.lenskit.featurizer;

import org.lenskit.space.IndexSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnglishTokenizeIdToIdxExtractor implements FeatureExtractor {
    private final String indexName;
    private final String attrName;
    private final String feaName;

    public EnglishTokenizeIdToIdxExtractor(String indexName,
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
            String[] tokens = attr.split(" ");
            for (String token : tokens) {
                String key = attrName + ":" + token;
                FeatureExtractorUtilities.getOrSetIndexSpaceToFeaturize(features, update,
                                                                        indexSpace, indexName, key);
            }
        }
        Map<String, List<Feature>> feaMap = new HashMap<>();
        feaMap.put(feaName, features);
        return feaMap;
    }
}
