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
package org.lenskit.basic;

import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.results.RescoredResult;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Basic rating predictor that uses scores, clamped to valid rating values if appropriate.  It returns results of type
 * {@link org.lenskit.results.RescoredResult}.
 *
 * The default item scorer used by the rating predictor is provided by {@link FallbackItemScorer.DynamicProvider}, so
 * it will default to falling back to the baseline scorer if one is present.
 */
public class SimpleRatingPredictor implements RatingPredictor {
    @Nonnull
    private final ItemScorer scorer;
    @Nullable
    private final PreferenceDomain preferenceDomain;

    /**
     * Create a new simple rating predictor.
     * @param s The scorer.
     * @param dom The preference domain.
     */
    @Inject
    public SimpleRatingPredictor(@Nonnull @PredictionScorer ItemScorer s,
                                 @Nullable PreferenceDomain dom) {
        scorer = s;
        preferenceDomain = dom;
    }

    @Nonnull
    public ItemScorer getItemScorer() {
        return scorer;
    }

    @Nullable
    public PreferenceDomain getPreferenceDomain() {
        return preferenceDomain;
    }

    @Override
    public RescoredResult predict(long user, long item) {
        Result r = scorer.score(user, item);
        if(r == null) {
            return null;
        }
        double val = r.getScore();
        if (preferenceDomain != null) {
            val = preferenceDomain.clampValue(val);
        }
        return Results.rescore(r, val);
    }

    @Nonnull
    @Override
    public Map<Long, Double> predict(long user, @Nonnull Collection<Long> items) {
        Map<Long, Double> scores = scorer.score(user, items);
        if (preferenceDomain == null) {
            return scores;
        } else {
            return preferenceDomain.clampVector(scores);
        }
    }

    @Nonnull
    @Override
    public ResultMap predictWithDetails(long user, @Nonnull Collection<Long> items) {
        ResultMap scores = scorer.scoreWithDetails(user, items);
        List<Result> rescored = new ArrayList<>(scores.size());
        for (Result r: scores) {
            double val = r.getScore();
            if (preferenceDomain != null) {
                val = preferenceDomain.clampValue(val);
            }
            rescored.add(Results.rescore(r, val));
        }
        return Results.newResultMap(rescored);
    }
}
