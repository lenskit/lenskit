/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.lenskit.featurizer;

import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;
import org.lenskit.space.IndexSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A feature extractor that takes a long type input entity attribute and one-hot encode it with appropriate
 * index in the given index space and using feature value as one.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
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

    /**
     * Convert a long-typed attribute in entity to be a one-hot encoded categorical feature with feature value one.
     * The attrName together with its attribute value (i.e. a long integer) are treated as the key. It will extract
     * out a feature except when attrName is not in entity, or attrName is not in indexSpace and update is false.
     *
     * @param entity the data entity to extract feature from.
     * @param indexSpace the look-up index space to use
     * @param update whether update indexSpace if the used attributes are not present in the indexSpace
     * @return a feature map from feature names to a list of features.
     */
    public Map<String, List<Feature>> extract(Entity entity, boolean update,
                                              IndexSpace indexSpace) {
        Map<String, List<Feature>> feaMap = new HashMap<>();
        if (entity.hasAttribute(attrName)) {
            List<Feature> features = new ArrayList<>();
            Long attr = entity.get(TypedName.create(attrName, Long.class));
            String key = attrName + "=" + attr.toString();
            FeatureExtractorUtilities.getOrSetIndexSpaceToFeaturize(features, update,
                                                                    indexSpace, indexName, key, 1.0);
            feaMap.put(feaName, features);
        }
        return feaMap;
    }
}
