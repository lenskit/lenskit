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
package org.lenskit.data.ratings;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.grouplens.lenskit.core.Transient;
import org.lenskit.util.io.ObjectStream;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.lenskit.util.keys.KeyedObjectMap;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * An efficient rating summary builder that does not acknowledge rerate/unrate events.
 *
 * @since 3.0
 */
public class FastRatingSummaryBuilder implements Provider<RatingSummary> {
    private final EventDAO eventDAO;

    @Inject
    public FastRatingSummaryBuilder(@Transient EventDAO dao) {
        eventDAO = dao;
    }

    @Override
    public RatingSummary get() {
        Long2DoubleMap sums = new Long2DoubleOpenHashMap();
        Long2IntMap counts = new Long2IntOpenHashMap();
        double totalSum = 0;
        int totalCount = 0;

        try (ObjectStream<Rating> ratings = eventDAO.streamEvents(Rating.class)) {
            for (Rating r: ratings) {
                if (r.hasValue()) {
                    long item = r.getItemId();
                    counts.put(item, counts.get(item) + 1);
                    sums.put(item, sums.get(item) + r.getValue());
                    totalSum += r.getValue();
                    totalCount += 1;
                }
            }
        }

        List<RatingSummary.ItemSummary> summaries = new ArrayList<>(sums.size());

        for (Long2DoubleMap.Entry e: sums.long2DoubleEntrySet()) {
            long item = e.getLongKey();
            double sum = e.getDoubleValue();
            int count = counts.get(item);
            summaries.add(new RatingSummary.ItemSummary(item, sum / count, count));
        }
        return new RatingSummary(totalSum / totalCount, KeyedObjectMap.create(summaries));
    }
}
