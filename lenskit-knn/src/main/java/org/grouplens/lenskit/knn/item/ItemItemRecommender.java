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

import javax.inject.Inject;

import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.core.ScoreBasedItemRecommender;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;

/**
 * Recommend items using item-item collaborative filtering.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class ItemItemRecommender extends ScoreBasedItemRecommender {
    protected final ItemItemModelBackedScorer scorer;

    /**
     * Construct a new item-item recommender from a scorer.
     *
     * @param scorer The scorer to use.
     */
    @Inject
    public ItemItemRecommender(DataAccessObject dao, ItemItemModelBackedScorer scorer) {
        super(dao, scorer);
        this.scorer = scorer;
    }

    @Override
    public LongSet getPredictableItems(UserHistory<? extends Event> user) {
        return scorer.getScoreableItems(user);
    }
}
