/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
 * Score items for users.  These scores can be predicted ratings, relevance
 * scores, purchase probabilities, or any other real-valued score which can be
 * assigned to an item for a particular user.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 * @since 0.4
 */
public interface ItemScorer {
    /**
     * Score a single item.
     *
     * @param user The user ID for whom to generate a score.
     * @param item The item ID to score.
     * @return The score, or {@link Double#NaN} if no score can be generated.
     */
    double score(long user, long item);

    /**
     * Score a collection of items.
     *
     * @param user  The user ID for whom to generate scores.
     * @param items The item to score.
     * @return A mapping from item IDs to predicted preference. This mapping may
     *         not contain all requested items.
     */
    @Nonnull
    SparseVector score(long user, @Nonnull Collection<Long> items);

    /**
     * Score items in a vector. The key domain of the provided vector is the
     * items to score, and the score method sets the values for each item to
     * its score (or unsets it, if no score can be provided). The previous
     * values are discarded.
     *
     * @param user   The user ID.
     * @param scores The score vector.
     */
    void score(long user, @Nonnull MutableSparseVector scores);
}
