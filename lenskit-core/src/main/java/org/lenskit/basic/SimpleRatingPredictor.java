package org.lenskit.basic;

import org.grouplens.lenskit.data.pref.PreferenceDomain;
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
