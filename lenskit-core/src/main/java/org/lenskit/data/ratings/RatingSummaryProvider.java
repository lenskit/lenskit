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
package org.lenskit.data.ratings;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.inject.Transient;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.keys.HashKeyIndex;
import org.lenskit.util.keys.SortedKeyIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Default builder for rating summaries.
 *
 * @since 3.0
 */
public class RatingSummaryProvider implements Provider<RatingSummary> {
    private static final Logger logger = LoggerFactory.getLogger(RatingSummaryProvider.class);
    private final DataAccessObject dao;

    @Inject
    public RatingSummaryProvider(@Transient DataAccessObject dao) {
        this.dao = dao;
    }

    @Override
    public RatingSummary get() {
        HashKeyIndex index = new HashKeyIndex();
        DoubleArrayList sums = new DoubleArrayList();
        IntArrayList counts = new IntArrayList();

        double totalSum = 0;
        int totalCount = 0;

        try (ObjectStream<Rating> ratings = dao.query(Rating.class).stream()) {
            for (Rating r: ratings) {
                assert sums.size() == counts.size();
                long item = r.getItemId();
                int idx = index.internId(item);
                if (idx >= sums.size()) {
                    assert idx == sums.size() && idx == counts.size();
                    sums.add(r.getValue());
                    counts.add(1);
                } else {
                    sums.set(idx, sums.getDouble(idx) + r.getValue());
                    counts.set(idx, counts.getInt(idx) + 1);
                }
                totalSum += r.getValue();
                totalCount += 1;
            }
        }

        double mean = totalCount > 0 ? totalSum / totalCount : 0;

        SortedKeyIndex items = SortedKeyIndex.fromCollection(index.getKeyList());
        final int n = items.size();
        int[] countArray = new int[n];
        double[] offsets = new double[n];

        for (int i = 0; i < n; i++) {
            int oidx = index.getIndex(items.getKey(i));
            countArray[i] = counts.getInt(oidx);
            offsets[i] = sums.getDouble(oidx) / countArray[i] - mean;
        }

        logger.info("summarized {} items with {} ratings", sums.size(), totalCount);

        return new RatingSummary(mean, items, offsets, countArray);
    }
}
