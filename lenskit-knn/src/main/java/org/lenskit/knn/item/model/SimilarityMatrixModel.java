/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.knn.item.model;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.util.keys.SortedKeyIndex;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Item-item similarity model using an in-memory similarity matrix.
 *
 * <p>
 * These similarities are post-normalization, so code using them
 * should use the same normalizations used by the builder to make use of the
 * similarity scores.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
@DefaultProvider(ItemItemModelBuilder.class)
@Shareable
public class SimilarityMatrixModel implements Serializable, ItemItemModel {
    private static final long serialVersionUID = 3L;

    private final SortedKeyIndex itemDomain;
    private final ImmutableList<ImmutableSparseVector> neighborhoods;
    private transient volatile String stringValue;

    /**
     * Construct a new item-item model.
     *
     * @param items The item domain.
     * @param nbrs  The item neighborhoods.
     * @deprecated This is deprecated for public usage.  It is better to use the other constructor.
     */
    @Deprecated
    public SimilarityMatrixModel(SortedKeyIndex items, List<ImmutableSparseVector> nbrs) {
        itemDomain = items;
        neighborhoods = ImmutableList.copyOf(nbrs);
    }

    /**
     * Construct a new item-item model.
     *
     * @param nbrs  The item neighborhoods.  The item neighborhood lists are not copied.
     */
    public SimilarityMatrixModel(Map<Long,ImmutableSparseVector> nbrs) {
        itemDomain = SortedKeyIndex.fromCollection(nbrs.keySet());
        int n = itemDomain.size();
        assert n == nbrs.size();
        ImmutableList.Builder<ImmutableSparseVector> neighbors = ImmutableList.builder();
        for (int i = 0; i < n; i++) {
            neighbors.add(nbrs.get(itemDomain.getKey(i)));
        }
        neighborhoods = neighbors.build();
    }

    @Override
    public LongSortedSet getItemUniverse() {
        return itemDomain.keySet();
    }

    @Override
    @Nonnull
    public SparseVector getNeighbors(long item) {
        int idx = itemDomain.getIndex(item);
        if (idx < 0) {
            return ImmutableSparseVector.empty();
        } else {
            return neighborhoods.get(idx);
        }
    }

    @Override
    public String toString() {
        String val = stringValue;
        if (val == null) {
            int nsims = 0;
            for (SparseVector nbrs: neighborhoods) {
                nsims += nbrs.size();
            }
            val = String.format("matrix of %d similarities for %d items", nsims, neighborhoods.size());
            stringValue = val;
        }
        return val;
    }
}
