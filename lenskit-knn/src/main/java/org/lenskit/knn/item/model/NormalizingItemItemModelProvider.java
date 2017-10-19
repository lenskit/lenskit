/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.knn.item.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.transform.truncate.VectorTruncator;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.ItemSimilarity;
import org.lenskit.transform.normalize.ItemVectorNormalizer;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.SortedKeyIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

/**
 * Build an item-item CF model from rating data.
 * This builder is more advanced than the standard builder. It allows arbitrary
 * vector truncation and normalization.
 */
public class NormalizingItemItemModelProvider implements Provider<ItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(NormalizingItemItemModelProvider.class);

    private final ItemSimilarity similarity;
    private final ItemItemBuildContext buildContext;
    private final ItemVectorNormalizer rowNormalizer;
    private final VectorTruncator truncator;
    private final NeighborIterationStrategy iterationStrategy;

    /**
     * Construct a normalizing item-item model builder.
     *
     * @param sim     The item similarity function.
     * @param context The item-item build context.
     * @param rowNorm The normalizer for item neighborhood vectors.
     * @param trunc   The truncator for truncating neighborhood vectors.  Bind this to the provider
     *                {@link StandardVectorTruncatorProvider} to get the same threshold and model
     *                size configuration behavior as {@link ItemItemModelProvider}.
     * @param iterStrat The neighbor iteration strategy.
     */
    @Inject
    public NormalizingItemItemModelProvider(@Transient ItemSimilarity sim,
                                            @Transient ItemItemBuildContext context,
                                            @Transient ItemVectorNormalizer rowNorm,
                                            @Transient VectorTruncator trunc,
                                            @Transient NeighborIterationStrategy iterStrat) {
        similarity = sim;
        buildContext = context;
        rowNormalizer = rowNorm;
        truncator = trunc;
        iterationStrategy = iterStrat;
    }


    @Override
    public SimilarityMatrixModel get() {
        logger.debug("building item-item model");

        LongSortedSet itemUniverse = buildContext.getItems();

        final int nitems = itemUniverse.size();

        SortedKeyIndex itemDomain = SortedKeyIndex.fromCollection(itemUniverse);
        assert itemDomain.size() == nitems;
        List<Long2DoubleMap> matrix = Lists.newArrayListWithCapacity(itemDomain.size());

        // working space for accumulating each row (reuse between rows)
        Stopwatch timer = Stopwatch.createStarted();

        for (int i = 0; i < nitems; i++) {
            assert matrix.size() == i;
            final long rowItem = itemDomain.getKey(i);
            final Long2DoubleSortedMap vec1 = buildContext.itemVector(rowItem);

            // Take advantage of sparsity if we can
            LongIterator neighbors = iterationStrategy.neighborIterator(buildContext, rowItem, false);
            Long2DoubleMap row = new Long2DoubleOpenHashMap(itemDomain.size());

            // Compute similarities and populate the vector
            while (neighbors.hasNext()) {
                final long colItem = neighbors.nextLong();
                if (colItem == rowItem) {
                    continue;
                }
                final Long2DoubleSortedMap vec2 = buildContext.itemVector(colItem);
                row.put(colItem, similarity.similarity(rowItem, vec1, colItem, vec2));
            }

            // Normalize and truncate the row
            row = rowNormalizer.makeTransformation(rowItem, row).apply(row);
            row = truncator.truncate(row);

            matrix.add(LongUtils.frozenMap(row));
        }

        timer.stop();
        logger.info("built model for {} items in {}", nitems, timer);

        return new SimilarityMatrixModel(itemDomain, matrix);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(NormalizingItemItemModelProvider.class)
                          .add("similarity", similarity)
                          .add("normalizer", rowNormalizer)
                          .add("truncator", truncator)
                          .toString();
    }
}
