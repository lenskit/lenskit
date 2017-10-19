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
 * Score items for users.  These scores can be predicted ratings, relevance
 * scores, purchase probabilities, or any other real-valued score which can be
 * assigned to an item for a particular user.
 *
 *  @compat Public
 */
public interface ItemScorer {
    /**
     * Score a single item.
     *
     * @param user The user ID for whom to generate a score.
     * @param item The item ID to score.
     * @return The score, or `null` if no score can be generated.
     */
    Result score(long user, long item);

    /**
     * Score a collection of items.
     *
     * @param user  The user ID for whom to generate scores.
     * @param items The item to score.
     * @return The scores for the items. This result set may not contain all requested items.
     */
    @Nonnull
    Map<Long,Double> score(long user, @Nonnull Collection<Long> items);

    /**
     * Score a collection of items and potentially return more details on the scores.
     *
     * @param user  The user ID for whom to generate scores.
     * @param items The item to score.
     * @return The scores for the items. This result set may not contain all requested items.  Implementations that
     * support additional details will return a subclass of {@link ResultMap} that provides access to those details.
     */
    @Nonnull
    ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items);
}
