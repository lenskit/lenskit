/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
        LongArrays.quickSort(items, new AbstractLongComparator() {
            @Override
            public int compare(long l1, long l2) {
                return Integer.compare(stats.getInteractionCount(l2), stats.getInteractionCount(l1));
            }
        });
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
