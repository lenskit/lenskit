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
