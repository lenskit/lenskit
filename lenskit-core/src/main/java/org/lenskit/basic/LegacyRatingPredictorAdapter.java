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

import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LegacyRatingPredictorAdapter implements RatingPredictor {
    private final org.grouplens.lenskit.RatingPredictor delegate;

    @Inject
    public LegacyRatingPredictorAdapter(org.grouplens.lenskit.RatingPredictor old) {
        delegate = old;
    }

    @Override
    public Result predict(long user, long item) {
        double predict = delegate.predict(user, item);
        if (Double.isNaN(predict)) {
            return null;
        } else {
            return Results.create(item, predict);
        }
    }

    @Nonnull
    @Override
    public Map<Long, Double> predict(long user, @Nonnull Collection<Long> items) {
        return predictWithDetails(user, items).scoreMap();
    }

    @Nonnull
    @Override
    public ResultMap predictWithDetails(long user, @Nonnull Collection<Long> items) {
        SparseVector res = delegate.predict(user, items);
        List<Result> results = new ArrayList<>(res.size());
        for (VectorEntry e: res) {
            results.add(Results.create(e.getKey(), e.getValue()));
        }
        return Results.newResultMap(results);
    }
}
