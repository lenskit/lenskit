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
package org.grouplens.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdBuilder;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
@DefaultProvider(ItemItemModelBuilder.class)
@Shareable
public class SimilarityMatrixModel implements Serializable, ItemItemModel {
    private static final long serialVersionUID = 2L;

    private final Long2ObjectMap<ImmutableSparseVector> similarityMatrix;
    private final LongSortedSet itemUniverse;

    /**
     * Construct a new item-item model.
     *
     * @param universe The set of item IDs. This should be equal to the key set
     *                 of the matrix.
     * @param matrix   The similarity matrix columns (maps item ID to column)
     */
    public SimilarityMatrixModel(LongSortedSet universe, Long2ObjectMap<ImmutableSparseVector> matrix) {
        itemUniverse = universe;
        similarityMatrix = matrix;
    }

    @Override
    public LongSortedSet getItemUniverse() {
        return itemUniverse;
    }

    @Override
    @Nonnull
    public ImmutableSparseVector getNeighbors(long item) {
        ImmutableSparseVector v = similarityMatrix.get(item);
        if (v == null) {
            return new MutableSparseVector().freeze();
        } else {
            return v;
        }
    }
}
