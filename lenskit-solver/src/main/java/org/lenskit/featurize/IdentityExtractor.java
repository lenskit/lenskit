package org.lenskit.featurize;

import org.lenskit.space.IndexSpace;
import org.lenskit.space.VariableSpace;

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
                                              IndexSpace indexSpace,
                                              VariableSpace variableSpace) {
        List<Feature> feaList = new ArrayList<>();
        if (entity.hasNumAttr(attrName)) {
            double val = entity.getNumAttr(attrName);
            if (indexSpace.containsStringKey(indexName, attrName)) {
                Feature feature = new Feature(
                        indexSpace.getIndexForStringKey(indexName, attrName), val);
                feaList.add(feature);
            } else if (update) {
                int index = indexSpace.setStringKey(indexName, attrName);
                Feature feature = new Feature(index, val);
                feaList.add(feature);
            }
        }
        Map<String, List<Feature>> feaMap = new HashMap<>();
        feaMap.put(feaName, feaList);
        return feaMap;
    }
}
