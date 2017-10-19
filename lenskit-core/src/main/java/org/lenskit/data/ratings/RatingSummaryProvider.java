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
