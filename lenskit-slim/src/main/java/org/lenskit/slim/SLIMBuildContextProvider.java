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
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.MinCommonUsers;
import org.lenskit.similarity.VectorSimilarity;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.lenskit.util.IdBox;
import org.lenskit.util.ProgressLogger;
import org.lenskit.util.collections.Long2DoubleAccumulator;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.collections.TopNLong2DoubleAccumulator;
import org.lenskit.util.collections.UnlimitedLong2DoubleAccumulator;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;
import java.util.Map;

/**
 * SLIM model build context provider using all item ratings as neighbors to train SLIM model
 * Build the necessary context for SLIM model
 * including user-item rating map, item neighbors map, item-item inner-products map and users' rated items
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SLIMBuildContextProvider implements Provider<SLIMBuildContext> {
    private static final Logger logger = LoggerFactory.getLogger(SLIMBuildContextProvider.class);

    private final RatingVectorPDAO rvDAO;
    private final UserVectorNormalizer normalizer;


    @Inject
    public SLIMBuildContextProvider(@Transient RatingVectorPDAO dao,
                                    @Transient UserVectorNormalizer normlzr) {
        this.rvDAO = dao;
        normalizer = normlzr;

    }


    /**
     * Construct the data objects needed by building slim model.
     * @return The slimBuildContext.
     */
    @Override
    public SLIMBuildContext get() {
        Long2ObjectMap<Long2DoubleSortedMap> itemVectors = new Long2ObjectOpenHashMap<>();
        Long2ObjectMap<LongSortedSet> userItems = new Long2ObjectOpenHashMap<>();
        buildItemRatings(itemVectors, userItems);

        Long2ObjectMap<Long2DoubleSortedMap> innerProducts = new Long2ObjectOpenHashMap<>();
        buildItemItemInnerProducts(itemVectors, innerProducts);

        Long2ObjectMap<LongSortedSet> itemNeighbors = new Long2ObjectOpenHashMap<>();
        buildItemNeighbors(itemVectors, itemNeighbors);

        return new SLIMBuildContext(itemVectors, itemNeighbors, innerProducts, userItems);
    }


    /**
     * Build item rating matrix
     * @param itemVectors mapping from item ids to (userId: rating) maps to be filled
     * @param userItems mapping from user ids to sets of user rated items (to be filled)
     */
    private void buildItemRatings(Long2ObjectMap<Long2DoubleSortedMap> itemVectors,
                                  Long2ObjectMap<LongSortedSet> userItems) {
        // initialize the transposed array to collect item vector data
        Long2ObjectMap<Long2DoubleMap> itemRatings = new Long2ObjectOpenHashMap<>();
        try (ObjectStream<IdBox<Long2DoubleMap>> stream = rvDAO.streamUsers()) {
            for (IdBox<Long2DoubleMap> user : stream) {
                long uid = user.getId();
                Long2DoubleMap ratings = user.getValue();
                Long2DoubleMap normed = normalizer.makeTransformation(uid, ratings).apply(ratings);
                assert normed != null;

                Iterator<Long2DoubleMap.Entry> iter = Vectors.fastEntryIterator(normed);
                while (iter.hasNext()) {
                    Long2DoubleMap.Entry rating = iter.next();
                    final long item = rating.getLongKey();
                    // get the item's rating accumulator
                    Long2DoubleMap ivect = itemRatings.get(item);
                    if (ivect == null) {
                        ivect = new Long2DoubleOpenHashMap(100);
                        itemRatings.put(item, ivect);
                    }
                    ivect.put(uid, rating.getDoubleValue());
                }

                // get the item's candidate set
                userItems.put(uid, LongUtils.packedSet(normed.keySet()));
            }
        }

        Iterator<Long2ObjectMap.Entry<Long2DoubleMap>> iter = itemRatings.long2ObjectEntrySet().iterator();
        while (iter.hasNext()) {
            Long2ObjectMap.Entry<Long2DoubleMap> entry = iter.next();
            long item = entry.getLongKey();
            Long2DoubleMap ratings = entry.getValue();
            itemVectors.put(item, LongUtils.frozenMap(ratings));
            iter.remove();
        }
    }


    /**
     * Build item-item inner-products matrix used to speed up SLIM learning process.
     * @param itemVectors mapping from item ids to (userId: rating) maps
     * @param innerProds mapping from item ids to (itemId: inner-product) maps to be filled
     */
    private void buildItemItemInnerProducts(Long2ObjectMap<Long2DoubleSortedMap> itemVectors, Long2ObjectMap<Long2DoubleSortedMap> innerProds) {
        Long2ObjectMap<Long2DoubleMap> innerProducts = new Long2ObjectOpenHashMap<>();
        LongSortedSet itemSet = LongUtils.frozenSet(itemVectors.keySet());
        LongIterator iter = itemSet.iterator();

        while (iter.hasNext()) {
            long itemIId = iter.nextLong();
            Long2DoubleMap itemIRatings = itemVectors.get(itemIId);

            //symmetric inner-products, so loop over the items after current item id
            LongIterator iterInner = itemSet.iterator(itemIId);
            while (iterInner.hasNext()) {
                long itemJId = iterInner.nextLong();
                Long2DoubleMap itemJRatings = itemVectors.get(itemJId);
                double innerProduct = Vectors.dotProduct(itemIRatings, itemJRatings);

                // storing interProducts used for SLIM learning
                Long2DoubleMap dotJIs = innerProducts.get(itemJId);
                Long2DoubleMap dotIJs = innerProducts.get(itemIId);
                if (dotJIs == null) dotJIs = new Long2DoubleOpenHashMap();
                if (dotIJs == null) dotIJs = new Long2DoubleOpenHashMap();
                dotJIs.put(itemIId, innerProduct);
                dotIJs.put(itemJId, innerProduct);
                innerProducts.put(itemJId, dotJIs);
                innerProducts.put(itemIId, dotIJs);
            }
        }
        //frozen each map in innerProducts and fill up innerProds
        Iterator<Map.Entry<Long,Long2DoubleMap>> innerProductsIter = innerProducts.entrySet().iterator();
        while (innerProductsIter.hasNext()) {
            Map.Entry<Long,Long2DoubleMap> entry = innerProductsIter.next();
            long item = entry.getKey();
            Long2DoubleMap innerProduct = entry.getValue();
            Long2DoubleSortedMap value = LongUtils.frozenMap(innerProduct);
            innerProds.put(item, value);
            innerProductsIter.remove();
        }
    }

    /**
     * Build mapping from item Ids to sets of similar items
     * @param itemVectors mapping from item ids to (userId: rating) maps
     * @param itemNeighbors mapping from items ids to sets of similar items (to be filled)
     */
    private void buildItemNeighbors(Long2ObjectMap<Long2DoubleSortedMap> itemVectors, Long2ObjectMap<LongSortedSet> itemNeighbors) {
        LongSortedSet allItems = LongUtils.frozenSet(itemVectors.keySet());

        LongIterator iter = allItems.iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            LongOpenHashSet itemNgbrCopied = new LongOpenHashSet(allItems);
            itemNgbrCopied.remove(item);
            itemNeighbors.put(item, LongUtils.frozenSet(itemNgbrCopied));
        }
    }
}
