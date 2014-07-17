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
package org.grouplens.lenskit;

import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Predict user ratings.  A rating predictor is like an {@link ItemScorer}, but its output will be
 * predicted ratings.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public interface RatingPredictor {
    /**
     * Predict a user's rating for a single item.
     *
     * @param user The user ID for whom to generate a prediction.
     * @param item The item ID whose rating is to be predicted.
     * @return The predicted preference, or {@link Double#NaN} if no preference can be
     *         predicted.
     */
    double predict(long user, long item);

    /**
     * Predict the user's preference for a collection of items.
     *
     * @param user  The user ID for whom to generate predicts.
     * @param items The items to predict for.
     * @return A mapping from item IDs to predicted preference. This mapping may
     *         not contain all requested items.
     */
    @Nonnull
    SparseVector predict(long user, @Nonnull Collection<Long> items);

    /**
     * Predict for items in a vector. The key domain of the provided vector is the items whose
     * predictions are requested, and the predict method sets the values for each item to its
     * predict (or unsets it, if no prediction can be provided). The previous values are discarded.
     *
     * @param user        The user ID.
     * @param predictions The prediction output vector.  Its key domain is the items to score.
     */
    void predict(long user, @Nonnull MutableSparseVector predictions);
}
