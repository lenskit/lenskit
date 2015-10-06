package org.lenskit.test;

import org.lenskit.api.ItemScorer;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

class TestComponent implements RatingPredictor {
    private ItemScorer scorer;

    @Inject
    public TestComponent(ItemScorer s) {
        scorer = s;
    }

    @Nonnull
    @Override
    public ResultMap predictWithDetails(long user, @Nonnull Collection<Long> items) {
        return scorer.scoreWithDetails(user, items);
    }

    @Override
    public Result predict(long user, long item) {
        return scorer.score(user, item);
    }

    @Nonnull
    @Override
    public Map<Long, Double> predict(long user, @Nonnull Collection<Long> items) {
        return scorer.score(user, items);
    }
}
