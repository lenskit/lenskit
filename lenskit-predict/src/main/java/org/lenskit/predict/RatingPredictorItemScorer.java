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

import org.lenskit.api.ItemScorer;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

/**
 * Item scorer that uses rating predictions.  Use this if you want to use the outputs of a
 * sophisticated rating predictor somewhere that requires item scorers.
 *
 * @since 2.1
 */
public class RatingPredictorItemScorer implements ItemScorer {
    private final RatingPredictor predictor;

    @Inject
    public RatingPredictorItemScorer(RatingPredictor pred) {
        predictor = pred;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        return predictor.predictWithDetails(user, items);
    }

    @Override
    public Result score(long user, long item) {
        return predictor.predict(user, item);
    }

    @Nonnull
    @Override
    public Map<Long, Double> score(long user, @Nonnull Collection<Long> items) {
        return predictor.predict(user, items);
    }
}
