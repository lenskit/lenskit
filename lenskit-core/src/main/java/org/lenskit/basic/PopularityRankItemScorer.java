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
package org.lenskit.basic;

import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.data.ratings.InteractionStatistics;
import org.lenskit.inject.Shareable;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Item scorer scores items based on their popularity rank.  1 is the most popular item, and 0 is an unknown item.
 */
@Shareable
public class PopularityRankItemScorer extends AbstractItemScorer implements Serializable {
    private static final long serialVersionUID = 2L;

    private final InteractionStatistics statistics;
    private final Long2DoubleSortedArrayMap rankScores;

    @Inject
    public PopularityRankItemScorer(final InteractionStatistics stats) {
        statistics = stats;
        long[] items = stats.getKnownItems().toLongArray();
        LongArrays.quickSort(items, (l1, l2) -> Integer.compare(stats.getInteractionCount(l2), stats.getInteractionCount(l1)));
        Long2IntMap ranks = LongUtils.itemRanks(LongArrayList.wrap(items));
        SortedKeyIndex keys = SortedKeyIndex.fromCollection(ranks.keySet());
        int n = keys.size();
        double[] values = new double[n];
        for (int i = 0; i < n; i++) {
            values[i] = 1.0 - ranks.get(keys.getKey(i)) / ((double) n);
        }
        rankScores = Long2DoubleSortedArrayMap.wrap(keys, values);
    }

    @Nonnull
    @Override
    public Map<Long, Double> score(long user, @Nonnull Collection<Long> items) {
        return rankScores.subMap(LongUtils.asLongSet(items));
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        List<Result> results = new ArrayList<>(items.size());
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            long item = iter.nextLong();
            results.add(Results.create(item, rankScores.get(item)));
        }
        return Results.newResultMap(results);
    }
}
