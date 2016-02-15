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
package org.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.lenskit.api.ResultList;
import org.lenskit.data.dao.UserEventDAO;
import org.lenskit.data.events.Event;
import org.lenskit.data.history.UserHistory;
import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Item recommender that just recommends the most popular items.
 */
public class PopularItemRecommender extends AbstractItemRecommender {
    private final RatingSummary summary;
    private final UserEventDAO userEvents;

    @Inject
    PopularItemRecommender(RatingSummary rs, @Nullable UserEventDAO uedao) {
        summary = rs;
        userEvents = uedao;
    }

    @Override
    protected ResultList recommendWithDetails(long user, int n, @Nullable LongSet candidates, @Nullable LongSet exclude) {
        if (candidates == null) {
            candidates = summary.getItems();
        }
        if (exclude == null) {
            UserHistory<Event> profile = null;
            if (userEvents != null) {
                profile = userEvents.getEventsForUser(user);
            }
            if (profile != null) {
                exclude = profile.itemSet();
            } else {
                exclude = LongSets.EMPTY_SET;
            }
        }

        LongSet effCand = exclude == null ? candidates : LongUtils.setDifference(candidates, exclude);
        ScoredItemAccumulator accum = ScoredItemAccumulator.create(n);
        LongIterator iter = effCand.iterator();
        while (iter.hasNext()) {
            long item = iter.nextLong();
            accum.put(item, summary.getItemRatingCount(item));
        }
        return Results.newResultList(accum.finishResults());
    }
}
