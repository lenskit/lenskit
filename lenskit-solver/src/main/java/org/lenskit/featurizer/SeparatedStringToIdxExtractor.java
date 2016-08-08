package org.lenskit.featurizer;

import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;
import org.lenskit.space.IndexSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeparatedStringToIdxExtractor implements FeatureExtractor {
    private final String indexName;
    private final String attrName;
    private final String feaName;
    private final String separator;

    public SeparatedStringToIdxExtractor(String indexName,
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
        Map<String, List<Feature>> feaMap = new HashMap<>();
        if (entity.hasAttribute(attrName)) {
            List<Feature> features = new ArrayList<>();
            String attr = entity.get(TypedName.create(attrName, String.class));
            String[] fields = attr.split(separator);
            for (String field : fields) {
                String key = attrName + "=" + field;
                FeatureExtractorUtilities.getOrSetIndexSpaceToFeaturize(features, update,
                                                                        indexSpace, indexName, key);
            }
            feaMap.put(feaName, features);
        }
        return feaMap;
    }
}
