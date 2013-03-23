package org.grouplens.lenskit.basic;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Basic {@link org.grouplens.lenskit.RatingPredictor} backed by an
 * {@link org.grouplens.lenskit.ItemScorer}.  The scores are clamped to the preference domain
 * but otherwise unmodified.
 *
 * @author Michael Ekstrand
 * @since 1.1
 */
public final class SimpleRatingPredictor extends AbstractRatingPredictor {
    private final ItemScorer scorer;
    @Nullable
    private final PreferenceDomain domain;

    @Inject
    public SimpleRatingPredictor(ItemScorer scorer, @Nullable PreferenceDomain domain) {
        super(null); // safe to use null ctor, as we override all methods that need it.
        this.scorer = scorer;
        this.domain = domain;
    }

    @Override
    public void predict(long user, @Nonnull MutableSparseVector scores) {
        scorer.score(user, scores);
        if (domain != null) {
            domain.clampVector(scores);
        }
    }

    @Override
    public double predict(long user, long item) {
        double v = scorer.score(user, item);
        if (domain != null) {
            v = domain.clampValue(v);
        }
        return v;
    }

    @Override
    public double predict(@Nonnull UserHistory<? extends Event> profile, long item) {
        double v = scorer.score(profile, item);
        if (domain != null) {
            v = domain.clampValue(v);
        }
        return v;
    }

    @Override
    public void predict(@Nonnull UserHistory<? extends Event> profile, @Nonnull MutableSparseVector predictions) {
        scorer.score(profile, predictions);
        if (domain != null) {
            domain.clampVector(predictions);
        }
    }
}
