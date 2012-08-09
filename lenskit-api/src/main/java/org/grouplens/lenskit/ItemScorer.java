/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import java.util.Collection;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Score items for users.  These scores can be predicted ratings, relevance
 * scores, purchase probabilities, or any other real-valued score which can be
 * assigned to an item for a particular user.
 *
 * <p>
 * This method provides two flavors of score methods: those that take a user ID,
 * loading data from the database as appropriate, and those that take a user
 * history.  Some recommenders may ignore the history and use data pre-computed
 * in a model for the user.  The {@link #canUseHistory()} method allows client
 * code to query whether the history can actually be used by the underlying
 * recommender.  Both methods will work, but if {@link #canUseHistory()} returns
 * <tt>false</tt>, then the history-based methods will be equivalent to calling
 * the ID-based methods.
 *
 * @since 0.4
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface ItemScorer {
    /**
     * Score a single item.
     *
     * @param user The user ID for whom to generate a score.
     * @param item The item ID to score.
     * @return The preference, or {@link Double#NaN} if no preference can be
     *         predicted.
     */
    double score(long user, long item);

    /**
     * Score a collection of items.
     *
     * @param user The user ID for whom to generate scores.
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
     * @param user The user ID.
     * @param scores The score vector.
     */
    void score(long user, @Nonnull MutableSparseVector scores);

    /**
     * Query whether this scorer can actually use user history.
     *
     * @return <tt>true</tt> if the history passed to one of the history-based
     *         methods may be used, and <tt>false</tt> if it will be ignored.
     */
    boolean canUseHistory();

    /**
     * Score an item for the user using a history. If possible, the provided
     * history is used instead of whatever history may be in the database or
     * model.
     *
     * @param profile The user's profile.
     * @param item The item to score.
     * @return The score, or {@link Double#NaN} if no score can be computed.
     */
    double score(@Nonnull UserHistory<? extends Event> profile, long item);

    /**
     * Score a collection of items for the user using a history. If possible,
     * the provided history is used instead of whatever history may be in the
     * database or model.
     *
     * @param profile The user's profile
     * @param items The items to score.
     * @return A mapping from item IDs to scores. This mapping may not contain
     *         all requested items — ones for which the scorer cannot compute a
     *         score will be omitted.
     */
    @Nonnull
    SparseVector score(@Nonnull UserHistory<? extends Event> profile,
                       @Nonnull Collection<Long> items);

    /**
     * Score items in a vector. The key domain of the provided vector is the
     * items to score, and the score method sets the values for each item to
     * its score (or unsets it, if no score can be provided). The previous
     * values are discarded.
     * @param profile The user history.
     * @param scores The score vector.
     */
    void score(@Nonnull UserHistory<? extends Event> profile,
               @Nonnull MutableSparseVector scores);
}
