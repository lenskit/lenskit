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

/**
 * Score items for users.  These scores can be predicted ratings, relevance
 * scores, purchase probabilities, or any other real-valued score which can be
 * assigned to an item for a particular user.
 *
 * @compat Public
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
    ResultMap<Result> score(long user, @Nonnull Collection<Long> items);

    /**
     * Score a collection of items and potentially return more details on the scores.
     *
     * @param user  The user ID for whom to generate scores.
     * @param items The item to score.
     * @return The scores for the items. This result set may not contain all requested items.  Implementations that
     * support additional details will return a subclass of {@link ResultMap} that provides access to those details.
     */
    @Nonnull
    ResultMap<? extends Result> scoreWithDetails(long user, @Nonnull Collection<Long> items);
}
