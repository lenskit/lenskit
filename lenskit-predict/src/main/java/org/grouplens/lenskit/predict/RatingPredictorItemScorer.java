package org.grouplens.lenskit.predict;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Item scorer that uses rating predictions.  Use this if you want to use the outputs of a
 * sophisticated rating predictor somewhere that requires item scorers.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RatingPredictorItemScorer implements ItemScorer {
    private final RatingPredictor predictor;

    @Inject
    public RatingPredictorItemScorer(RatingPredictor pred) {
        predictor = pred;
    }

    @Override
    public double score(long user, long item) {
        return predictor.predict(user, item);
    }

    @Nonnull
    @Override
    public SparseVector score(long user, @Nonnull Collection<Long> items) {
        return predictor.predict(user, items);
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        predictor.predict(user, scores);
    }
}
