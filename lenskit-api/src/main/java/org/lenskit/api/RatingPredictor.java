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
package org.lenskit.api;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * Predict user ratings.  A rating predictor is like an {@link ItemScorer}, but its output is scaled or otherwise
 * transformed for rating prediction.  An item score can be anything that meets the requirement 'higher is better';
 * a rating prediction can be interpreted as an estimate of the user's expected rating, within the system's stated
 * range of valid ratings.  Depending on the predictor used, rating predictions may also be quantized.
 *
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
    Result predict(long user, long item);

    /**
     * Predict the user's preference for a collection of items.
     *
     * @param user  The user ID for whom to generate predicts.
     * @param items The items to predict for.
     * @return A mapping from item IDs to predicted preference. This mapping may
     *         not contain all requested items.
     */
    @Nonnull
    Map<Long,Double> predict(long user, @Nonnull Collection<Long> items);

    /**
     * Predict the user's preference for a collection of items, potentially with additional details.
     *
     * @param user  The user ID for whom to generate predicts.
     * @param items The items to predict for.
     * @return A mapping from item IDs to predicted preference. This mapping may
     *         not contain all requested items.
     */
    @Nonnull
    ResultMap predictWithDetails(long user, @Nonnull Collection<Long> items);
}
