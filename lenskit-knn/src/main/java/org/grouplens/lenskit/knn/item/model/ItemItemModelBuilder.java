/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.knn.item.model;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.knn.item.ItemSimilarity;
import org.grouplens.lenskit.knn.item.ItemSimilarityThreshold;
import org.grouplens.lenskit.knn.item.ModelSize;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.util.UnlimitedScoredItemAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Build an item-item CF model from rating data.
 * This builder takes a very simple approach. It does not allow for vector
 * normalization and truncates on the fly.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@NotThreadSafe
public class ItemItemModelBuilder implements Provider<ItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemModelBuilder.class);

    private final ItemSimilarity itemSimilarity;
    private final ItemItemBuildContext buildContext;
    private final Threshold threshold;
    private final NeighborIterationStrategy neighborStrategy;
    private final int modelSize;

    @Inject
    public ItemItemModelBuilder(@Transient ItemSimilarity similarity,
                                @Transient ItemItemBuildContext context,
                                @Transient @ItemSimilarityThreshold Threshold thresh,
                                @Transient NeighborIterationStrategy nbrStrat,
                                @ModelSize int size) {
        itemSimilarity = similarity;
        buildContext = context;
        threshold = thresh;
        neighborStrategy = nbrStrat;
        modelSize = size;
    }

    @Override
    public SimilarityMatrixModel get() {
        logger.debug("building item-item model");
        logger.debug("using similarity function {}", itemSimilarity);
        logger.debug("similarity function is {}",
                     itemSimilarity.isSparse() ? "sparse" : "non-sparse");
        logger.debug("similarity function is {}",
                     itemSimilarity.isSymmetric() ? "symmetric" : "non-symmetric");

        LongSortedSet allItems = buildContext.getItems();

        Long2ObjectMap<ScoredItemAccumulator> rows = makeAccumulators(allItems);

        final int nitems = allItems.size();
        LongIterator outer = allItems.iterator();

        Stopwatch timer = Stopwatch.createStarted();
        int ndone = 0;
        while (outer.hasNext()) {
            ndone += 1;
            final long itemId1 = outer.nextLong();
            if (logger.isTraceEnabled()) {
                logger.trace("computing similarities for item {} ({} of {})",
                             itemId1, ndone, nitems);
            }
            SparseVector vec1 = buildContext.itemVector(itemId1);

            LongIterator itemIter = neighborStrategy.neighborIterator(buildContext, itemId1,
                                                                      itemSimilarity.isSymmetric());

            ScoredItemAccumulator row = rows.get(itemId1);
            while (itemIter.hasNext()) {
                long itemId2 = itemIter.nextLong();
                if (itemId1 != itemId2) {
                    SparseVector vec2 = buildContext.itemVector(itemId2);
                    double sim = itemSimilarity.similarity(itemId1, vec1, itemId2, vec2);
                    if (threshold.retain(sim)) {
                        row.put(itemId2, sim);
                        if (itemSimilarity.isSymmetric()) {
                            rows.get(itemId2).put(itemId1, sim);
                        }
                    }
                }
            }

            if (logger.isDebugEnabled() && ndone % 100 == 0) {
                logger.debug("computed {} of {} model rows ({}s/row)",
                             ndone, nitems,
                             String.format("%.3f", timer.elapsed(TimeUnit.MILLISECONDS) * 0.001 / ndone));
            }
        }
        timer.stop();
        logger.info("built model for {} items in {}", ndone, timer);

        return new SimilarityMatrixModel(finishRows(rows));
    }

    private Long2ObjectMap<ScoredItemAccumulator> makeAccumulators(LongSet items) {
        Long2ObjectMap<ScoredItemAccumulator> rows = new Long2ObjectOpenHashMap<ScoredItemAccumulator>(items.size());
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long item = iter.nextLong();
            ScoredItemAccumulator accum;
            if (modelSize == 0) {
                accum = new UnlimitedScoredItemAccumulator();
            } else {
                accum = new TopNScoredItemAccumulator(modelSize);
            }
            rows.put(item, accum);
        }
        return rows;
    }

    private Long2ObjectMap<List<ScoredId>> finishRows(Long2ObjectMap<ScoredItemAccumulator> rows) {
        Long2ObjectMap<List<ScoredId>> results = new Long2ObjectOpenHashMap<List<ScoredId>>(rows.size());
        for (Long2ObjectMap.Entry<ScoredItemAccumulator> e: CollectionUtils.fast(rows.long2ObjectEntrySet())) {
            results.put(e.getLongKey(), e.getValue().finish());
        }
        return results;
    }
}
