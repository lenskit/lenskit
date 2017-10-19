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
