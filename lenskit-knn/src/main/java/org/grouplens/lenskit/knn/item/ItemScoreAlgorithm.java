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
package org.grouplens.lenskit.knn.item;

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.knn.item.model.ItemItemModel;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Algorithm for scoring items given an item-item model and neighborhood scorer. Used by {@link
 * ItemItemScorer} and {@link ItemItemGlobalScorer} to score items. This logic is mainly abstracted
 * so that the personalized and global item scorers can share logic; it isn't very common to
 * reimplement this component.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
@DefaultImplementation(DefaultItemScoreAlgorithm.class)
public interface ItemScoreAlgorithm {
    /**
     * Score items for a user.
     *
     * @param model    The item-item model.
     * @param userData The user's rating data.
     * @param scores   The score vector (key domain is items to score). Unscoreable items will be
     *                 left unchanged.
     * @param scorer   The scorer to use.
     */
    void scoreItems(ItemItemModel model, SparseVector userData,
                    MutableSparseVector scores,
                    NeighborhoodScorer scorer);
}
