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
package org.grouplens.lenskit.knn.model;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.io.Serializable;

import javax.annotation.Nonnull;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.collections.ScoredLongArrayList;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.core.Shareable;

/**
 * Item-item similarity model using an in-memory similarity matrix.
 *
 * <p/>
 * These similarities are post-normalization, so code using them
 * should use the same normalizations used by the builder to make use of the
 * similarity scores.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @since 0.10
 */
@DefaultProvider(ItemItemModelProvider.class)
@Shareable
public class SimilarityMatrixModel implements Serializable, ItemItemModel {
    private static final long serialVersionUID = -5986236982760043379L;

    private static final ScoredLongList EMPTY_LIST = new ScoredLongArrayList();

    private final Long2ObjectMap<ScoredLongList> similarityMatrix;
    private final LongSortedSet itemUniverse;

    /**
     * Construct a new item-item model.
     * @param universe The set of item IDs. This should be equal to the key set
     *                 of the matrix.
     * @param matrix The similarity matrix columns (maps item ID to column)
     */
    public SimilarityMatrixModel(LongSortedSet universe, Long2ObjectMap<ScoredLongList> matrix) {
        itemUniverse = universe;
        similarityMatrix = matrix;
    }

    @Override
    public LongSortedSet getItemUniverse() {
        return itemUniverse;
    }

    @Override
    @Nonnull
    public ScoredLongList getNeighbors(long item) {
        ScoredLongList nbrs = similarityMatrix.get(item);
        if (nbrs == null) {
            nbrs = EMPTY_LIST;
        }
        return nbrs;
    }
}
