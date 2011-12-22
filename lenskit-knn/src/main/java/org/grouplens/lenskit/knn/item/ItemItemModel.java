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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.io.Serializable;

import org.grouplens.lenskit.collections.ScoredLongArrayList;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.params.meta.Built;
import org.grouplens.lenskit.params.meta.DefaultBuilder;

/**
 * Item-item similarity model. It stores and makes available the similarities
 * between items. These similarities are post-normalization, so code using them
 * should use the same normalizations used by the builder to make use of the
 * similarity scores.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Built
@DefaultBuilder(ItemItemModelBuilder.class)
public class ItemItemModel implements Serializable {
    private static final long serialVersionUID = -5986236982760043379L;

    private static final ScoredLongList EMPTY_LIST = new ScoredLongArrayList();

    private final Long2ObjectMap<ScoredLongList> similarityMatrix;
    private final LongSortedSet itemUniverse;

    public ItemItemModel(LongSortedSet universe, Long2ObjectMap<ScoredLongList> matrix) {
        itemUniverse = universe;
        similarityMatrix = matrix;
    }

    public LongSortedSet getItemUniverse() {
        return itemUniverse;
    }

    public ScoredLongList getNeighbors(long item) {
        ScoredLongList nbrs = similarityMatrix.get(item);
        if (nbrs == null)
            nbrs = EMPTY_LIST;
        return nbrs;

    }
}
