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

import org.grouplens.grapht.annotation.Transient;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.knn.item.ItemItemBuildContext;
import org.grouplens.lenskit.knn.item.ItemItemBuildContextFactory;
import org.grouplens.lenskit.params.Damping;
import org.grouplens.lenskit.util.Indexer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Pre-computes the deviations and number of mutual rating users for every pair
 *  of items and stores the results in a <tt>DeviationMatrix</tt> and
 *  <tt>CoratingMatrix</tt>. These matrices are later used by a
 *  <tt>SlopeOneRatingPredictor</tt>.
 */
public class SlopeOneModelProvider implements Provider<SlopeOneModel> {
    private final SlopeOneModelDataAccumulator accumulator;

    private final BaselinePredictor predictor;
    private final PreferenceDomain domain;
    private final ItemItemBuildContextFactory contextFactory;
    private final Indexer itemIndex;

    @Inject
    public SlopeOneModelProvider(@Transient @Nonnull DataAccessObject dao,
                                 @Nullable BaselinePredictor predictor,
                                 @Nonnull PreferenceDomain domain,
                                 @Transient ItemItemBuildContextFactory contextFactory,
                                 @Damping double damping) {

        this.predictor = predictor;
        this.domain = domain;
        this.contextFactory = contextFactory;
        itemIndex = new Indexer();
        accumulator = new SlopeOneModelDataAccumulator(damping, itemIndex, dao);
    }

    /**
     * Constructs and returns a {@link SlopeOneModel}.
     */
    @Override
    public SlopeOneModel get() {
        ItemItemBuildContext buildContext = contextFactory.buildContext();
        for (ItemItemBuildContext.ItemVecPair pair : buildContext.getItemPairs()) {
            if (pair.itemId1 != pair.itemId2) {
                accumulator.putItemPair(pair.itemId1, pair.vec1, pair.itemId2, pair.vec2);
            }
        }
        return new SlopeOneModel(accumulator.buildCoratingMatrix(), accumulator.buildDeviationMatrix(),
                predictor, itemIndex, domain);
    }
}
