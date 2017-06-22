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
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.MinCommonUsers;
import org.lenskit.similarity.VectorSimilarity;
import org.lenskit.transform.normalize.ItemVectorNormalizer;
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
import java.util.List;
import java.util.Map;

/**
 * SLIM model build context provider
 * Build the necessary context for SLIM model
 * including user-item rating map, item neighbors map, item-item inner-products map and users' rated items
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SLIMBuildContextProvider implements Provider<SLIMBuildContext> {
    private static final Logger logger = LoggerFactory.getLogger(SLIMBuildContextProvider.class);


    private final DataAccessObject dao;
    private final ItemVectorNormalizer normalizer;
    private final VectorSimilarity itemSimilarity;
    private final Threshold threshold;
    private final int minCommonUsers;
    private final int modelSize;

    @Inject
    public SLIMBuildContextProvider(@Transient DataAccessObject dao,
                                    @Transient ItemVectorNormalizer normlzr,
                                    @Transient VectorSimilarity sim,
                                    @Transient Threshold threshld,
                                    @MinCommonUsers int minCI,
                                    @SLIMModelSize int size) {
        this.dao = dao;
        normalizer = normlzr;
        itemSimilarity = sim;
        threshold = threshld;
        minCommonUsers = minCI;
        modelSize = size;

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
        Long2ObjectMap<LongSet> userItemsTemp = new Long2ObjectOpenHashMap<>();
        try (ObjectStream<IdBox<List<Rating>>> stream = dao.query(Rating.class)
                                                           .groupBy(CommonAttributes.ITEM_ID)
                                                           .stream()) {
            for (IdBox<List<Rating>> item : stream) {
                long itemId = item.getId();
                List<Rating> itemRatings = item.getValue();
                Long2DoubleOpenHashMap ratings = new Long2DoubleOpenHashMap(Ratings.itemRatingVector(itemRatings));
                Long2DoubleMap normed = normalizer.makeTransformation(itemId, ratings).apply(ratings);
                assert normed != null;

                LongIterator iter = normed.keySet().iterator();
                while (iter.hasNext()) {
                    long userId = iter.nextLong();
                    LongSet userRatedItems = userItemsTemp.get(userId);
                    if (userRatedItems == null) userRatedItems = new LongOpenHashSet();
                    userRatedItems.add(itemId);
                    userItemsTemp.put(userId, userRatedItems);
                }

                itemVectors.put(itemId, LongUtils.frozenMap(normed));
            }
        }

        Iterator<Map.Entry<Long,LongSet>> userItemsIter = userItemsTemp.entrySet().iterator();
        while (userItemsIter.hasNext()) {
            Map.Entry<Long,LongSet> entry = userItemsIter.next();
            long item = entry.getKey();
            LongSortedSet value = LongUtils.frozenSet(entry.getValue());
            userItems.put(item, value);
            userItemsIter.remove();
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
        logger.info("building item-item model for {} items", itemVectors.keySet().size());
        logger.debug("using similarity function {}", itemSimilarity);
        logger.debug("similarity function is {}",
                itemSimilarity.isSparse() ? "sparse" : "non-sparse");
        logger.debug("similarity function is {}",
                itemSimilarity.isSymmetric() ? "symmetric" : "non-symmetric");

        LongSortedSet allItems = LongUtils.frozenSet(itemVectors.keySet());

        Long2ObjectMap<Long2DoubleAccumulator> rows = makeAccumulators(allItems);

        final int nitems = allItems.size();
        LongIterator outer = allItems.iterator();

        ProgressLogger progress = ProgressLogger.create(logger)
                .setCount(nitems)
                .setLabel("item-item model build")
                .setWindow(50)
                .start();
        int ndone = 0;
        int npairs = 0;
        OUTER: while (outer.hasNext()) {
            ndone += 1;
            final long itemId1 = outer.nextLong();
            if (logger.isTraceEnabled()) {
                logger.trace("computing similarities for item {} ({} of {})",
                        itemId1, ndone, nitems);
            }
            Long2DoubleSortedMap vec1 = LongUtils.frozenMap(itemVectors.get(itemId1));
            if (vec1.size() < minCommonUsers) {
                // if it doesn't have enough users, it can't have enough common users
                if (logger.isTraceEnabled()) {
                    logger.trace("item {} has {} (< {}) users, skipping", itemId1, vec1.size(), minCommonUsers);
                }
                progress.advance();
                continue OUTER;
            }

            LongIterator itemIter = itemSimilarity.isSymmetric() ? allItems.iterator(itemId1) : allItems.iterator();

            Long2DoubleAccumulator row = rows.get(itemId1);
            INNER: while (itemIter.hasNext()) {
                long itemId2 = itemIter.nextLong();
                if (itemId1 != itemId2) {
                    Long2DoubleSortedMap vec2 = LongUtils.frozenMap(itemVectors.get(itemId2));
                    if (!LongUtils.hasNCommonItems(vec1.keySet(), vec2.keySet(), minCommonUsers)) {
                        // items have insufficient users in common, skip them
                        continue INNER;
                    }

                    double sim = itemSimilarity.similarity(vec1, vec2);
                    if (threshold.retain(sim)) {
                        row.put(itemId2, sim);
                        npairs += 1;
                        if (itemSimilarity.isSymmetric()) {
                            rows.get(itemId2).put(itemId1, sim);
                            npairs += 1;
                        }
                    }
                }
            }

            progress.advance();
        }
        progress.finish();
        logger.info("built model of {} similarities for {} items in {}",
                npairs, ndone, progress.elapsedTime());

        Long2ObjectMap<LongSet> neighborSets = finishRows(rows);

        //frozen each set and fill up itemNeighbors
        Iterator<Map.Entry<Long,LongSet>> itemNeighborIter = neighborSets.entrySet().iterator();
        while (itemNeighborIter.hasNext()) {
            Map.Entry<Long,LongSet> entry = itemNeighborIter.next();
            long item = entry.getKey();
            LongSortedSet value = LongUtils.frozenSet(entry.getValue());
            itemNeighbors.put(item, value);
            itemNeighborIter.remove();
        }
    }

    private Long2ObjectMap<Long2DoubleAccumulator> makeAccumulators(LongSet items) {
        Long2ObjectMap<Long2DoubleAccumulator> rows = new Long2ObjectOpenHashMap<>(items.size());
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long item = iter.nextLong();
            Long2DoubleAccumulator accum;
            if (modelSize == 0) {
                accum = new UnlimitedLong2DoubleAccumulator();
            } else {
                accum = new TopNLong2DoubleAccumulator(modelSize);
            }
            rows.put(item, accum);
        }
        return rows;
    }

    private Long2ObjectMap<LongSet> finishRows(Long2ObjectMap<Long2DoubleAccumulator> rows) {
        Long2ObjectMap<LongSet> results = new Long2ObjectOpenHashMap<>(rows.size());
        for (Long2ObjectMap.Entry<Long2DoubleAccumulator> e: rows.long2ObjectEntrySet()) {
            results.put(e.getLongKey(), e.getValue().finishSet());
        }
        return results;
    }
}
