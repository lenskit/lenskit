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
/**
 *
 */
package org.lenskit.baseline;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.lenskit.inject.Shareable;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.results.Results;
import org.lenskit.util.math.Scalars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Rating scorer that returns the item's mean rating for all predictions.
 *
 * If the item has no ratings, the global mean rating is returned.
 *
 * This implements the baseline scorer \(p_{u,i} = \mu + b_i\),
 * where \(b_i\) is the item's average rating (less the global
 * mean \(\mu\)).
 */
@Shareable
public class ItemMeanRatingItemScorer extends AbstractItemScorer implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ItemMeanRatingItemScorer.class);

    private final RatingSummary summary;
    private final double damping;

    /**
     * Construct a new scorer. This assumes ownership of the provided map.
     *
     * @param summary The rating summary.
     * @param damping    The damping factor.
     */
    @Inject
    public ItemMeanRatingItemScorer(RatingSummary summary, @MeanDamping double damping) {
        Preconditions.checkArgument(damping >= 0, "Negative damping not allowed");
        this.summary = summary;
        this.damping = damping;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        final double gmean = summary.getGlobalMean();
        List<Result> results = new ArrayList<>();
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            double offset = summary.getItemOffset(item);
            if (!Scalars.isZero(damping)) {
                int count = summary.getItemRatingCount(item);
                offset = offset * count / (count + damping);
            }
            results.add(Results.create(item, gmean + offset));
        }
        return Results.newResultMap(results);
    }

    @Override
    public String toString() {
        String cls = getClass().getSimpleName();
        return String.format("%s(µ=%.3f, γ=%.2f)", cls, summary.getGlobalMean(), damping);
    }
}
