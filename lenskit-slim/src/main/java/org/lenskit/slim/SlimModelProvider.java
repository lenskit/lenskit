/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.slim;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.model.ItemItemModel;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;



public class SlimModelProvider implements Provider<SlimModel>{
    private static final Logger logger = LoggerFactory.getLogger(SlimModelProvider.class);

    private final ItemItemModel itemSimilarityModel;
    private final SlimBuildContext buildContext;
    private final AbstractLinearRegression lrModel;

    @Inject
    public SlimModelProvider(ItemItemModel m,
                             @Transient SlimBuildContext context,
                             AbstractLinearRegression regressionModel) {
        itemSimilarityModel = m;
        buildContext = context;
        lrModel = regressionModel;
    }

    @Override
    public SlimModel get() {
        Long2ObjectMap<Long2DoubleMap> trainedWeight = new Long2ObjectOpenHashMap<>(1000);
        LongSortedSet items = itemSimilarityModel.getItemUniverse();
        for (long item : items) {
            LongSet neighbors = LongUtils.frozenSet(itemSimilarityModel.getNeighbors(item).keySet());
            Map<Long,Long2DoubleMap> trainingData = Maps.newHashMap();
            Map<Long,Long2DoubleMap> innerProducts = Maps.newHashMap();
            Long2DoubleMap labels = LongUtils.frozenMap(buildContext.getItemRatings(item));
            for (long nbrs : neighbors) {
                trainingData.put(nbrs, LongUtils.frozenMap(buildContext.getItemRatings(nbrs)));
                innerProducts.put(nbrs, LongUtils.frozenMap(buildContext.getInnerProducts(nbrs)));
            }
            Long2DoubleMap weight = lrModel.fit(labels, trainingData, innerProducts, item);
            trainedWeight.put(item, weight);
        }
        return new SlimModel(trainedWeight);
    }
}
