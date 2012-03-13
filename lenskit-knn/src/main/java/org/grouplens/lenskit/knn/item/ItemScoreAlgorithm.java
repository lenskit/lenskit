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

import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.params.meta.DefaultClass;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Algorithm for scoring items given an item-item model and neighborhood scorer.
 * Used by {@link ItemItemScorer} and {@link ItemItemGlobalScorer} to score items.
 * @author Michael Ekstrand
 * @since 0.10
 */
@DefaultClass(DefaultItemScoreAlgorithm.class)
public interface ItemScoreAlgorithm {
    MutableSparseVector scoreItems(ItemItemModel model,
                                   SparseVector userData, LongSortedSet items,
                                   NeighborhoodScorer scorer);
}
