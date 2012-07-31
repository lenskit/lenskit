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
package org.grouplens.lenskit.slopeone;

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.knn.item.ItemItemBuildContext;
import org.grouplens.lenskit.knn.item.ModelBuildStrategy;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.Vectors;

/**
 * Slope One model build strategy. Constructs the co-ratings and
 * deviation matrices.
 */
public class SlopeOneModelBuildStrategy implements ModelBuildStrategy {


    public void buildModel(ItemItemBuildContext context,
                           SlopeOneModelDataAccumulator accumulator) {

        final LongSortedSet items = context.getItems();
        LongIterator itemIter = items.iterator();
        while (itemIter.hasNext()) {
            final long itemId = itemIter.nextLong();
            SparseVector itemVec = context.itemVector(itemId);

            LongIterator otherIter = items.iterator();
            while (otherIter.hasNext()) {
                final long otherId = otherIter.nextLong();
                if (itemId < otherId) {
                    SparseVector otherVec = context.itemVector(otherId);
                    for (Vectors.EntryPair coRating : Vectors.pairedFast(itemVec, otherVec)) {
                        accumulator.putRatingPair(itemId, coRating.getValue1(), otherId, coRating.getValue2());
                    }
                }
            }
        }
    }

    @Override
    public boolean needsUserItemSets() {
        return false;
    }
}
