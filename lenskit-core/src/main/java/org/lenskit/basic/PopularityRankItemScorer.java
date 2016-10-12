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
import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.inject.Shareable;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Item scorer scores items based on their popularity rank.  1 is the most popular item, and 0 is an unknown item.
 */
@Shareable
public class PopularityRankItemScorer extends AbstractItemScorer {
    private final RatingSummary summary;
    private final Long2IntMap ranks;

    @Inject
    PopularityRankItemScorer(RatingSummary rs) {
        summary = rs;
        long[] items = rs.getItems().toLongArray();
        LongArrays.quickSort(items, new AbstractLongComparator() {
            @Override
            public int compare(long l1, long l2) {
                return Integer.compare(summary.getItemRatingCount(l2), summary.getItemRatingCount(l1));
            }
        });
        ranks = LongUtils.itemRanks(LongArrayList.wrap(items));
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        List<Result> results = new ArrayList<>(items.size());
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            long item = iter.nextLong();
            int rank = ranks.get(item);
            results.add(Results.create(item, rankToScore(rank, ranks.size())));
        }
        return Results.newResultMap(results);
    }

    static double rankToScore(int rank, int n) {
        if (rank < 0) {
            return 0;
        } else {
            return 1.0 - rank / ((double) n);
        }
    }
}
