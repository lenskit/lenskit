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
 * A feature extractor that takes a numerical input entity attribute and directly output it as the feature value
 * while encoding feature name in a given index space.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class IdentityNumericalExtractor {
    private final String indexName;
    private final String attrName;
    private final String feaName;

    public IdentityNumericalExtractor(String indexName,
                              String attrName,
                              String feaName) {
        this.indexName = indexName;
        this.attrName = attrName;
        this.feaName = feaName;
    }

    public Map<String, List<Feature>> extract(Entity entity, boolean update,
                                              IndexSpace indexSpace) {
        Map<String, List<Feature>> feaMap = new HashMap<>();
        if (entity.hasAttribute(attrName)) {
            List<Feature> features = new ArrayList<>();
            double val = entity.get(TypedName.create(attrName, Double.class));
            FeatureExtractorUtilities.getOrSetIndexSpaceToFeaturize(features, update,
                                                                    indexSpace, indexName, attrName, val);
            feaMap.put(feaName, features);
        }
        return feaMap;
    }
}
