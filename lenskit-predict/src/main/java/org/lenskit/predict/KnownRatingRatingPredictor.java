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
package org.lenskit.predict;

import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractRatingPredictor;
import org.lenskit.basic.FallbackItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
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
    private final DataAccessObject dao;

    @Inject
    public KnownRatingRatingPredictor(DataAccessObject d) {
        dao = d;
    }

    @Nonnull
    @Override
    public ResultMap predictWithDetails(long user, @Nonnull Collection<Long> items) {
        List<Rating> ratings = dao.query(Rating.class)
                                  .withAttribute(CommonAttributes.USER_ID, user)
                                  .get();
        LongSortedSet wantedItems = LongUtils.packedSet(items);
        List<Result> results = new ArrayList<>();
        for (Rating r: ratings) {
            long item = r.getItemId();
            if (wantedItems.contains(r.getItemId())) {
                results.add(Results.create(item, r.getValue()));
            }
        }
        return Results.newResultMap(results);
    }
}
