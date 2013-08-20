package org.grouplens.lenskit.baseline;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Item scorer that combines a primary scorer with a baseline.  This scorer is comprised of two
 * other scorers, a primary scorer and a baseline scorer.  It first scores items using the primary
 * scorer, and then consults the baseline scorer for any items that the primary scorer could not
 * score.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class FallbackItemScorer extends AbstractItemScorer {
    private final ItemScorer primaryScorer;
    private final ItemScorer baselineScorer;

    @Inject
    public FallbackItemScorer(@PrimaryScorer ItemScorer primary,
                              @BaselineScorer ItemScorer baseline) {
        primaryScorer = primary;
        baselineScorer = baseline;
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector output) {
        primaryScorer.score(user, output);
        if (output.size() != output.keyDomain().size()) {
            MutableSparseVector blpreds = MutableSparseVector.create(output.unsetKeySet());
            baselineScorer.score(user, blpreds);
            output.set(blpreds);
        }
    }
}
