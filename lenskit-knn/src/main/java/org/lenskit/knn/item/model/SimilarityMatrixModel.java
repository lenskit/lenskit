/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.knn.item.model;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.api.ResultList;
import org.lenskit.inject.Shareable;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;
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
@DefaultProvider(ItemItemModelProvider.class)
@Shareable
public class SimilarityMatrixModel implements Serializable, ItemItemModel {
    private static final long serialVersionUID = 3L;

    private final SortedKeyIndex itemDomain;
    private final ImmutableList<Long2DoubleMap> neighborhoods;
    private transient volatile String stringValue;

    /**
     * Construct a new item-item model.
     *
     * @param items The item domain.
     * @param nbrs  The item neighborhoods.
     * @deprecated This is deprecated for public usage.  It is better to use the other constructor.
     */
    @Deprecated
    public SimilarityMatrixModel(SortedKeyIndex items, List<Long2DoubleMap> nbrs) {
        itemDomain = items;
        neighborhoods = ImmutableList.copyOf(nbrs);
    }

    /**
     * Construct a new item-item model.
     *
     * @param nbrs  The item neighborhoods.  The item neighborhood lists are not copied.
     */
    public SimilarityMatrixModel(Map<Long,Long2DoubleMap> nbrs) {
        itemDomain = SortedKeyIndex.fromCollection(nbrs.keySet());
        int n = itemDomain.size();
        assert n == nbrs.size();
        ImmutableList.Builder<Long2DoubleMap> neighbors = ImmutableList.builder();
        for (int i = 0; i < n; i++) {
            neighbors.add(LongUtils.frozenMap(nbrs.get(itemDomain.getKey(i))));
        }
        neighborhoods = neighbors.build();
    }

    @Override
    public LongSortedSet getItemUniverse() {
        return itemDomain.keySet();
    }

    @Override
    @Nonnull
    public Long2DoubleMap getNeighbors(long item) {
        int idx = itemDomain.tryGetIndex(item);
        if (idx < 0) {
            return Long2DoubleMaps.EMPTY_MAP;
        } else {
            return neighborhoods.get(idx);
        }
    }

    @Override
    public String toString() {
        String val = stringValue;
        if (val == null) {
            int nsims = 0;
            for (Long2DoubleMap nbrs: neighborhoods) {
                nsims += nbrs.size();
            }
            val = String.format("matrix of %d similarities for %d items", nsims, neighborhoods.size());
            stringValue = val;
        }
        return val;
    }
}
