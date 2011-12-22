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

import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;

/**
 * Item scorer specific to item-item recommenders. It exposes the item-item
 * model as well as the scoring functionality.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface ItemItemModelBackedScorer extends ItemScorer {
    /**
     * Get the item-item model backing this scorer.
     *
     * @return The model this scorer uses to compute scores.
     */
    ItemItemModel getModel();

    /**
     * Get the set of scoreable items for a user.
     * @param user The user to query for.
     * @return The set of items for which scores can be generated.
     */
    LongSet getScoreableItems(UserHistory<? extends Event> user);
}
