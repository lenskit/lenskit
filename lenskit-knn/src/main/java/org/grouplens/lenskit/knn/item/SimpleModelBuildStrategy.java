/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Similarity matrix strategy that assumes nothing about the similarity function.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class SimpleModelBuildStrategy implements ItemItemModelBuildStrategy {
    private final static Logger logger = LoggerFactory.getLogger(SimpleModelBuildStrategy.class);

    private final ItemSimilarity similarityFunction;

    SimpleModelBuildStrategy(ItemSimilarity similarity) {
        this.similarityFunction = similarity;
    }

    @Override
    public void buildModel(ItemItemBuildContext context,
                           SimilarityMatrixAccumulator accum) {
        final boolean symmetric = similarityFunction.isSymmetric();
        logger.debug("Building {} model", symmetric ? "symmetric" : "asymmetric");
        LongSortedSet items = context.getItems();
        LongIterator itemIter = items.iterator();
        while (itemIter.hasNext()) {
            final long itemId = itemIter.nextLong();
            // if it is symmetric, trim the item list.
            LongIterator otherIter = symmetric ? items.iterator(itemId) : items.iterator();
            while (otherIter.hasNext()) {
                final long otherId = otherIter.nextLong();
                if (itemId == otherId) continue;
                double sim =
                        similarityFunction.similarity(otherId, context.itemVector(otherId),
                                                      itemId, context.itemVector(itemId));
                accum.put(itemId, otherId, sim);
                if (symmetric) {
                    accum.put(otherId, itemId, sim);
                }
            }
        }
    }

    @Override
    public boolean needsUserItemSets() {
        return false;
    }
}
