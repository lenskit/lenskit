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

import org.grouplens.lenskit.core.Transient;
import org.lenskit.util.io.ObjectStream;
import org.grouplens.lenskit.data.dao.ItemEventDAO;
import org.grouplens.lenskit.data.history.ItemEventCollection;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.lenskit.util.keys.KeyedObjectMap;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * Default builder for rating summaries.  This builder is correct, but may be inefficient.  If your data set does not
 * have any unrate events, consider using {@link FastRatingSummaryBuilder}:
 *
 * ```java
 * config.bind(RatingSummary.class)
 *       .toProvider(FastRatingSummaryBuilder.class);
 * ```
 *
 * @since 3.0
 */
public class RatingSummaryBuilder implements Provider<RatingSummary> {
    private final ItemEventDAO itemEventDAO;

    @Inject
    public RatingSummaryBuilder(@Transient ItemEventDAO dao) {
        itemEventDAO = dao;
    }

    @Override
    public RatingSummary get() {
        double totalSum = 0;
        int totalCount = 0;
        List<RatingSummary.ItemSummary> summaries = new ArrayList<>();

        try (ObjectStream<ItemEventCollection<Rating>> ratings = itemEventDAO.streamEventsByItem(Rating.class)) {
            for (ItemEventCollection<Rating> item: ratings) {
                MutableSparseVector vec = Ratings.itemRatingVector(item);
                int n = vec.size();
                double sum = vec.sum();
                double mean = vec.mean();
                totalSum += sum;
                totalCount += n;
                summaries.add(new RatingSummary.ItemSummary(item.getItemId(), mean, n));
            }
        }

        return new RatingSummary(totalSum / totalCount, KeyedObjectMap.create(summaries));
    }
}
