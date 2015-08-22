package org.lenskit.baseline;

import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.lenskit.basic.ConstantItemScorer;
import org.lenskit.data.summary.RatingSummary;

import javax.inject.Inject;

/**
 * Item scorer that scores every item with the global mean rating.  It is not useful to use this scorer for
 * ranking items.
 */
@Shareable
public class GlobalMeanRatingItemScorer extends ConstantItemScorer {
    private static final long serialVersionUID = 1L;

    @Inject
    public GlobalMeanRatingItemScorer(@Transient RatingSummary summary) {
        super(summary.getGlobalMean());
    }
}
