package org.lenskit.featurizer;

import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;
import org.lenskit.space.IndexSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LongToIdxExtractor implements FeatureExtractor {
    private final String indexName;
    private final String attrName;
    private final String feaName;

    public LongToIdxExtractor(String indexName,
                                String attrName,
                                String feaName) {
        this.indexName = indexName;
        this.attrName = attrName;
        this.feaName = feaName;
    }

    public Map<String, List<Feature>> extract(Entity entity, boolean update,
                                              IndexSpace indexSpace) {
        List<Feature> features = new ArrayList<>();
        Long attr = entity.get(TypedName.create(attrName, Long.class));
        String key = attrName + "=" + attr.toString();
        FeatureExtractorUtilities.getOrSetIndexSpaceToFeaturize(features, update,
                                                                indexSpace, indexName, key);
        Map<String, List<Feature>> feaMap = new HashMap<>();
        feaMap.put(feaName, features);
        return feaMap;
    }
}
