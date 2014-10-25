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
package org.grouplens.lenskit.baseline;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.util.IdMeanAccumulator;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;

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
@DefaultProvider(ItemMeanRatingItemScorer.Builder.class)
@Shareable
public class ItemMeanRatingItemScorer extends AbstractItemScorer implements Serializable {
    /**
     * A builder to create ItemMeanPredictors.
     *
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder implements Provider<ItemMeanRatingItemScorer> {
        private double damping = 0;
        private EventDAO dao;

        /**
         * Construct a new provider.
         *
         * @param dao     The DAO.
         * @param damping The Bayesian mean damping term. It biases means toward the
         *                global mean.
         */
        @Inject
        public Builder(@Transient EventDAO dao,
                       @MeanDamping double damping) {
            this.dao = dao;
            this.damping = damping;
        }

        @Override
        public ItemMeanRatingItemScorer get() {
            final ImmutableSparseVector itemMeans;
            final double globalMean;

            logger.debug("computing item mean ratings");
            Cursor<Rating> ratings = dao.streamEvents(Rating.class);
            try {
                IdMeanAccumulator accum = new IdMeanAccumulator();
                for (Rating r: ratings) {
                    Preference p = r.getPreference();
                    if (p != null) {
                        accum.put(p.getItemId(), p.getValue());
                    }
                }
                globalMean = accum.globalMean();
                itemMeans = accum.idMeanOffsets(damping);
            } finally {
                ratings.close();
            }
            logger.debug("computed means for {} items", itemMeans.size());
            logger.debug("global mean rating is {}", globalMean);

            return new ItemMeanRatingItemScorer(itemMeans, globalMean, damping);
        }
    }

    private static final long serialVersionUID = 3L;
    private static final Logger logger = LoggerFactory.getLogger(ItemMeanRatingItemScorer.class);

    private final ImmutableSparseVector itemMeans;  // offsets from the global mean
    private final double globalMean;
    private final double damping;

    /**
     * Construct a new scorer. This assumes ownership of the provided map.
     *
     * @param itemMeans  A map of item IDs to their mean ratings.
     * @param globalMean The mean rating value for all items.
     * @param damping    The damping factor.
     */
    public ItemMeanRatingItemScorer(ImmutableSparseVector itemMeans, double globalMean, double damping) {
        this.itemMeans = itemMeans;
        this.globalMean = globalMean;
        this.damping = damping;
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector items) {
        items.fill(globalMean);
        items.add(itemMeans);
    }

    @Override
    public String toString() {
        String cls = getClass().getSimpleName();
        return String.format("%s(µ=%.3f, γ=%.2f)", cls, globalMean, damping);
    }
}
