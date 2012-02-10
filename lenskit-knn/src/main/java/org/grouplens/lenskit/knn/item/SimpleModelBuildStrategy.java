/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.util.SymmetricBinaryFunction;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Similarity matrix strategy that assumes nothing about the similarity function.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class SimpleModelBuildStrategy implements
        ItemItemModelBuildStrategy {
    private final static Logger logger = LoggerFactory.getLogger(SimpleModelBuildStrategy.class);

    private final Similarity<? super SparseVector> similarityFunction;

    SimpleModelBuildStrategy(Similarity<? super SparseVector> similarity) {
        this.similarityFunction = similarity;
    }

    @Override
    public void buildMatrix(ItemItemBuildContext context,
                            ItemItemModelAccumulator accum) {
        final boolean symmetric = similarityFunction instanceof SymmetricBinaryFunction;
        logger.debug("Building {} model", symmetric ? "symmetric" : "asymmetric");
        LongSortedSet items = context.getItems();
        LongIterator iit = items.iterator();
        while (iit.hasNext()) {
            final long i = iit.nextLong();
            // if it is symmetric, trim the item list.
            LongIterator jit = symmetric ? items.iterator(i) : items.iterator();
            while (jit.hasNext()) {
                final long j = jit.nextLong();
                if (i == j) continue;
                double sim =
                        similarityFunction.similarity(context.itemVector(j),
                                                      context.itemVector(i));
                accum.put(i, j, sim);
                if (symmetric) {
                    accum.put(j, i, sim);
                }
            }
        }
    }

    @Override
    public boolean needsUserItemSets() {
        return false;
    }
}
