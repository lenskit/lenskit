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

import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;
import org.lenskit.inject.Transient;
import org.lenskit.util.IdBox;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.math.Vectors;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * SLIM model build context provider
 * Build the necessary context for SLIM model
 * including user-item rating map, item-item inner-products map and users' rated items
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SLIMBuildContextProvider implements Provider<SLIMBuildContext> {
    private final DataAccessObject dao;

    /**
     * Construct the model provider.
     * @param dao The data access object.
     */
    @Inject
    public SLIMBuildContextProvider(@Transient DataAccessObject dao) {
        this.dao = dao;
    }

    /**
     * Construct the data objects needed by building slim model.
     * @return The slimBuildContext.
     */
    @Override
    public SLIMBuildContext get() {
        Long2ObjectMap<Long2DoubleMap> itemVectors = new Long2ObjectOpenHashMap<>();
        Long2ObjectMap<LongOpenHashBigSet> userItems = new Long2ObjectOpenHashMap<>();

        try (ObjectStream<IdBox<List<Rating>>> stream = dao.query(Rating.class)
                                                           .groupBy(CommonAttributes.ITEM_ID)
                                                           .stream()) {
            for (IdBox<List<Rating>> item : stream) {
                long itemId = item.getId();
                List<Rating> itemRatings = item.getValue();
                Long2DoubleOpenHashMap ratings = new Long2DoubleOpenHashMap(Ratings.itemRatingVector(itemRatings));

                LongIterator iter = ratings.keySet().iterator();
                while (iter.hasNext()) {
                    long userId = iter.nextLong();
                    LongOpenHashBigSet userRatedItems = userItems.get(userId);
                    if (userRatedItems == null) userRatedItems = new LongOpenHashBigSet();
                    userRatedItems.add(itemId);
                    userItems.put(userId, userRatedItems);
                }
                itemVectors.put(itemId, LongUtils.frozenMap(ratings));
            }
        }

        // Map items to vectors (maps) of item inner-product, which used to speed up slim learning process.
        Long2ObjectMap<Long2DoubleMap> innerProducts = new Long2ObjectOpenHashMap<>();
        LongOpenHashBigSet itemIdSet = new LongOpenHashBigSet(itemVectors.keySet());
        Iterator<Map.Entry<Long, Long2DoubleMap>> iter = itemVectors.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Long, Long2DoubleMap> entry = iter.next();
            long temIId = entry.getKey();
            Long2DoubleMap itemIRatings = entry.getValue();
            itemIdSet.remove(temIId);

            for (long itemJId : itemIdSet) {
                Long2DoubleMap itemJRatings = itemVectors.get(itemJId);
                double innerProduct = Vectors.dotProduct(itemIRatings, itemJRatings);

                // storing interProducts used for SLIM learning
                Long2DoubleMap dotJIs = innerProducts.get(itemJId);
                Long2DoubleMap dotIJs = innerProducts.get(temIId);
                if (dotJIs == null) dotJIs = new Long2DoubleOpenHashMap();
                if (dotIJs == null) dotIJs = new Long2DoubleOpenHashMap();
                dotJIs.put(temIId, innerProduct);
                dotIJs.put(itemJId, innerProduct);
                innerProducts.put(itemJId, dotJIs);
                innerProducts.put(temIId, dotIJs);
            }
        }

        return new SLIMBuildContext(itemVectors, innerProducts, userItems);
    }
}
