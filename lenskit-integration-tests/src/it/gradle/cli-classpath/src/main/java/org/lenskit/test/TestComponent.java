package org.lenskit.test;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.lang.Long;
import java.lang.Override;
import java.util.Collection;

class TestComponent implements RatingPredictor {
    private ItemScorer scorer;

    @Inject
    public TestComponent(ItemScorer s) {
        scorer = s;
    }

    @Override
    public double predict(long user, long item) {
        return scorer.score(user, item);
    }

    @Nonnull
    @Override
    public SparseVector predict(long user, @Nonnull Collection<Long> items) {
        return scorer.score(user, items);
    }

    @Override
    public void predict(long user, @Nonnull MutableSparseVector predictions) {
        scorer.score(user, predictions);
    }
}
