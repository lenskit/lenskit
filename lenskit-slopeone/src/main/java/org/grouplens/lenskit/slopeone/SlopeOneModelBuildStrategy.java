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
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.knn.item.ItemItemBuildContext;
import org.grouplens.lenskit.knn.item.ModelBuildStrategy;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

/**
 * Slope One model build strategy. Constructs
 */
public class SlopeOneModelBuildStrategy implements ModelBuildStrategy {


    public void buildModel(ItemItemBuildContext context,
             DataAccessObject dao, UserHistorySummarizer userSummarizer,
             SlopeOneModelDataAccumulator accumulator) {

        final LongSortedSet items = context.getItems();
        LongIterator itemIter = items.iterator();
        while (itemIter.hasNext()) {
            final long itemId = itemIter.nextLong();
            SparseVector itemVec = context.itemVector(itemId);

            final LongIterator userIter = itemVec.keySet().iterator();
            while (userIter.hasNext()) {
                final long userId = userIter.nextLong();

                final SparseVector userRatings = userSummarizer.summarize(dao.getUserHistory(userId));
                final double userItemRating = userRatings.get(itemId);
                for (VectorEntry userRating : userRatings.fast()) {
                    accumulator.putRatingPair(itemId, userItemRating,
                            userRating.getKey(), userRating.getValue());
                }
            }
        }
    }

    @Override
    public boolean needsUserItemSets() {
        return false;
    }
}
