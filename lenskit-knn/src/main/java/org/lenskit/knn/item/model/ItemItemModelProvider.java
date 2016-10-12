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
package org.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.lenskit.util.ScoredIdAccumulator;
import org.lenskit.util.TopNScoredIdAccumulator;
import org.lenskit.util.UnlimitedScoredIdAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.ItemSimilarity;
import org.lenskit.knn.item.ItemSimilarityThreshold;
import org.lenskit.knn.item.MinCommonUsers;
import org.lenskit.knn.item.ModelSize;
import org.lenskit.util.ProgressLogger;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Build an item-item CF model from rating data.
 * This builder takes a very simple approach. It does not allow for vector
 * normalization and truncates on the fly.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@NotThreadSafe
public class ItemItemModelProvider implements Provider<ItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemModelProvider.class);

    private final ItemSimilarity itemSimilarity;
    private final ItemItemBuildContext buildContext;
    private final Threshold threshold;
    private final NeighborIterationStrategy neighborStrategy;
    private final int minCommonUsers;
    private final int modelSize;

    @Inject
    public ItemItemModelProvider(@Transient ItemSimilarity similarity,
                                 @Transient ItemItemBuildContext context,
                                 @Transient @ItemSimilarityThreshold Threshold thresh,
                                 @Transient NeighborIterationStrategy nbrStrat,
                                 @MinCommonUsers int minCU,
                                 @ModelSize int size) {
        itemSimilarity = similarity;
        buildContext = context;
        threshold = thresh;
        neighborStrategy = nbrStrat;
        minCommonUsers = minCU;
        modelSize = size;
    }

    @Override
    public SimilarityMatrixModel get() {
        logger.info("building item-item model for {} items", buildContext.getItems().size());
        logger.debug("using similarity function {}", itemSimilarity);
        logger.debug("similarity function is {}",
                     itemSimilarity.isSparse() ? "sparse" : "non-sparse");
        logger.debug("similarity function is {}",
                     itemSimilarity.isSymmetric() ? "symmetric" : "non-symmetric");

        LongSortedSet allItems = buildContext.getItems();

        Long2ObjectMap<ScoredIdAccumulator> rows = makeAccumulators(allItems);

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
            SparseVector vec1 = buildContext.itemVector(itemId1);
            if (vec1.size() < minCommonUsers) {
                // if it doesn't have enough users, it can't have enough common users
                if (logger.isTraceEnabled()) {
                    logger.trace("item {} has {} (< {}) users, skipping", itemId1, vec1.size(), minCommonUsers);
                }
                progress.advance();
                continue OUTER;
            }

            LongIterator itemIter = neighborStrategy.neighborIterator(buildContext, itemId1,
                                                                      itemSimilarity.isSymmetric());

            ScoredIdAccumulator row = rows.get(itemId1);
            INNER: while (itemIter.hasNext()) {
                long itemId2 = itemIter.nextLong();
                if (itemId1 != itemId2) {
                    SparseVector vec2 = buildContext.itemVector(itemId2);
                    if (!LongUtils.hasNCommonItems(vec1.keySet(), vec2.keySet(), minCommonUsers)) {
                        // items have insufficient users in common, skip them
                        continue INNER;
                    }

                    double sim = itemSimilarity.similarity(itemId1, vec1, itemId2, vec2);
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

        return new SimilarityMatrixModel(finishRows(rows));
    }

    private Long2ObjectMap<ScoredIdAccumulator> makeAccumulators(LongSet items) {
        Long2ObjectMap<ScoredIdAccumulator> rows = new Long2ObjectOpenHashMap<>(items.size());
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long item = iter.nextLong();
            ScoredIdAccumulator accum;
            if (modelSize == 0) {
                accum = new UnlimitedScoredIdAccumulator();
            } else {
                accum = new TopNScoredIdAccumulator(modelSize);
            }
            rows.put(item, accum);
        }
        return rows;
    }

    private Long2ObjectMap<Long2DoubleMap> finishRows(Long2ObjectMap<ScoredIdAccumulator> rows) {
        Long2ObjectMap<Long2DoubleMap> results = new Long2ObjectOpenHashMap<>(rows.size());
        for (Long2ObjectMap.Entry<ScoredIdAccumulator> e: rows.long2ObjectEntrySet()) {
            results.put(e.getLongKey(), e.getValue().finishMap());
        }
        return results;
    }
}
