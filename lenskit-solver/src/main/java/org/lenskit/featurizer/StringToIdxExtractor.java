package org.lenskit.featurizer;

import org.lenskit.space.IndexSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringToIdxExtractor {
    private final String indexName;
    private final String attrName;
    private final String feaName;
    private final String separator;

    public StringToIdxExtractor(String indexName,
                                String attrName,
                                String feaName,
                                String separator) {
        this.indexName = indexName;
        this.attrName = attrName;
        this.feaName = feaName;
        this.separator = separator;
    }

    public Map<String, List<Feature>> extract(Entity entity, boolean update,
                                              IndexSpace indexSpace) {
        List<Feature> features = new ArrayList<>();
        List<String> attrs = entity.getCatAttr(attrName);
        for (String attr : attrs) {
            String[] fields = attr.split(separator);
            for (String field : fields) {
                String key = attrName + ":" + field;
                FeatureExtractorUtilities.getOrSetIndexSpaceToFeaturize(features, update,
                        indexSpace, indexName, key);
            }
        }
        Map<String, List<Feature>> feaMap = new HashMap<>();
        feaMap.put(feaName, features);
        return feaMap;
    }
}
