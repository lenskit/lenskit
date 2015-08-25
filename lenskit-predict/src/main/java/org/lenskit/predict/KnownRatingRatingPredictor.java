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
package org.lenskit.predict;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractRatingPredictor;
import org.lenskit.basic.FallbackItemScorer;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Use a user's existing ratings as predictions.  This rating predictor
 * returns the user's rating as a prediction for all items the user has rated,
 * and returns no prediction for items the user has rated. It is useful in
 * conjunction with a {@link FallbackItemScorer} to return a user's rating
 * when it is known.
 *
 * @see RatingPredictorItemScorer
 * @since 2.2
 */
public class KnownRatingRatingPredictor extends AbstractRatingPredictor {
    private final UserEventDAO dao;

    @Inject
    public KnownRatingRatingPredictor(UserEventDAO uedao) {
        this.dao = uedao;
    }

    @Nonnull
    @Override
    public ResultMap predictWithDetails(long user, @Nonnull Collection<Long> items) {
        List<Rating> ratings = dao.getEventsForUser(user, Rating.class);
        if (ratings == null) {
            return Results.newResultMap();
        }
        LongSortedSet wantedItems = LongUtils.packedSet(items);
        Long2ObjectMap<Result> results = new Long2ObjectOpenHashMap<>();
        for (Rating r: ratings) {
            long item = r.getItemId();
            if (wantedItems.contains(r.getItemId())) {
                if (r.hasValue()) {
                    results.put(item, Results.create(item, r.getValue()));
                } else {
                    results.remove(item);
                }
            }
        }
        return Results.newResultMap(results.values());
    }
}
