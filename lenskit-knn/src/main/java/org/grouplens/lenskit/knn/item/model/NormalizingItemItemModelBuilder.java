/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.knn.item.ItemSimilarity;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdListBuilder;
import org.grouplens.lenskit.scored.ScoredIds;
import org.grouplens.lenskit.transform.normalize.ItemVectorNormalizer;
import org.grouplens.lenskit.transform.truncate.VectorTruncator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
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
public class NormalizingItemItemModelBuilder implements Provider<ItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(NormalizingItemItemModelBuilder.class);

    private final ItemSimilarity similarity;
    private final ItemItemBuildContextFactory contextFactory;
    private final ItemVectorNormalizer rowNormalizer;
    private final VectorTruncator truncator;

    @Inject
    public NormalizingItemItemModelBuilder(@Transient ItemSimilarity similarity,
                                           @Transient ItemItemBuildContextFactory ctxFactory,
                                           @Transient ItemVectorNormalizer rowNormalizer,
                                           @Transient VectorTruncator truncator) {
        this.similarity = similarity;
        contextFactory = ctxFactory;
        this.rowNormalizer = rowNormalizer;
        this.truncator = truncator;
    }


    @Override
    public SimilarityMatrixModel get() {
        logger.debug("building item-item model");

        ItemItemBuildContext context = contextFactory.buildContext();
        MutableSparseVector currentRow = MutableSparseVector.create(context.getItems());

        LongSortedSet itemUniverse = context.getItems();
        Long2ObjectMap<List<ScoredId>> matrix =
                new Long2ObjectOpenHashMap<List<ScoredId>>(itemUniverse.size());

        LongIterator outer = context.getItems().iterator();
        while (outer.hasNext()) {
            final long rowItem = outer.nextLong();
            final SparseVector vec1 = context.itemVector(rowItem);
            for (VectorEntry e: currentRow.fast(VectorEntry.State.EITHER)) {
                final long colItem = e.getKey();
                final SparseVector vec2 = context.itemVector(colItem);
                currentRow.set(e, similarity.similarity(rowItem, vec1, colItem, vec2));
            }
            MutableSparseVector normalized = rowNormalizer.normalize(rowItem, currentRow, null);
            truncator.truncate(normalized);
            ScoredIdListBuilder bld = new ScoredIdListBuilder(normalized.size());
            // TODO Allow the symbols in use to be customized
            List<ScoredId> row = bld.addChannels(normalized.getChannelVectorSymbols())
                                    .addTypedChannels(normalized.getChannelSymbols())
                                    .addAll(ScoredIds.collectionFromVector(normalized))
                                    .sort(ScoredIds.scoreOrder().reverse())
                                    .finish();
            matrix.put(rowItem, row);
        }

        return new SimilarityMatrixModel(itemUniverse, matrix);
    }
}
