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
/**
 *
 */
package org.lenskit.baseline;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.grouplens.lenskit.baseline.MeanDamping;
import org.grouplens.lenskit.core.Shareable;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.summary.RatingSummary;
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
 * This implements the baseline scorer <i>p<sub>u,i</sub> = µ + b<sub>i</sub></i>,
 * where <i>b<sub>i</sub></i> is the item's average rating (less the global
 * mean µ).
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
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
