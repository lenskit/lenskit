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

import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Predict user ratings.  A rating predictor is like an {@link ItemScorer}, but its output will be
 * predicted ratings.
 * <p>
 *     <b>Note:</b> The fact that this interface extends {@link ItemScorer} is deprecated.
 * </p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public interface RatingPredictor extends ItemScorer {
    /**
     * Query whether this predictor can actually use user history.
     *
     * @return {@code true} if the history passed to one of the history-based
     *         methods may be used, and {@code false} if it will be ignored.
     */
    @Override
    boolean canUseHistory();

    /**
     * Prdict a user's rating for a single item.
     *
     * @param user The user ID for whom to generate a prediction.
     * @param item The item ID whose rating is to be predicted.
     * @return The predicted preference, or {@link Double#NaN} if no preference can be
     *         predicted.
     * @see #predict(UserHistory, MutableSparseVector)
     */
    double predict(long user, long item);

    /**
     * Predict the user's preference for a collection of items.
     *
     * @param user  The user ID for whom to generate predicts.
     * @param items The items to predict for.
     * @return A mapping from item IDs to predicted preference. This mapping may
     *         not contain all requested items.
     * @see #predict(UserHistory, MutableSparseVector)
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
     * @see #predict(UserHistory, MutableSparseVector)
     */
    void predict(long user, @Nonnull MutableSparseVector predictions);

    /**
     * Predict a user's preference for an item using a history. If possible, the provided history is
     * used instead of whatever history may be in the database or model.
     *
     * @param profile The user's profile.
     * @param item    The item to predict.
     * @return The predict, or {@link Double#NaN} if no predict can be computed.
     * @see #predict(UserHistory, MutableSparseVector)
     */
    double predict(@Nonnull UserHistory<? extends Event> profile, long item);

    /**
     * Predict a collection of items for the user using a history. If possible, the provided history
     * is used instead of whatever history may be in the database or model.
     *
     * @param profile The user's profile
     * @param items   The items to predict.
     * @return A mapping from item IDs to predicts. This mapping may not contain all requested items
     *         — ones for which the predictr cannot compute a predict will be omitted.
     * @see #predict(UserHistory, MutableSparseVector)
     */
    @Nonnull
    SparseVector predict(@Nonnull UserHistory<? extends Event> profile,
                         @Nonnull Collection<Long> items);

    /**
     * Predict for items in a vector. The key domain of the provided vector is the items whose
     * ratings should be predicted, and the predict method sets the values for each item to its
     * predict (or unsets it, if no prediction can be provided). The previous values are discarded.
     * <p> If the user has rated any items to be predicted, the algorithm should not just use their
     * rating as the predict — it should compute a predict in the normal fashion. If client code
     * wants to substitute ratings, it is easy to do so as a separate step or wrapper interface.
     *
     * @param profile     The user history.
     * @param predictions The prediction output vector.
     */
    void predict(@Nonnull UserHistory<? extends Event> profile,
                 @Nonnull MutableSparseVector predictions);

    /**
     * {@inheritDoc}
     * @deprecated Use {@link #predict(long, long)}
     */
    @Deprecated
    @Override
    double score(long user, long item);

    /**
     * {@inheritDoc}
     * @deprecated Use {@link #predict(long, Collection)}
     */
    @Deprecated
    @Nonnull
    @Override
    SparseVector score(long user, @Nonnull Collection<Long> items);

    /**
     * {@inheritDoc}
     * @deprecated Use {@link #predict(long, MutableSparseVector)}
     */
    @Deprecated
    @Override
    void score(long user, @Nonnull MutableSparseVector scores);

    /**
     * {@inheritDoc}
     * @deprecated Use {@link #predict(UserHistory, long)}
     */
    @Deprecated
    @Override
    double score(@Nonnull UserHistory<? extends Event> profile, long item);

    /**
     * {@inheritDoc}
     * @deprecated Use {@link #predict(UserHistory, Collection)}
     */
    @Deprecated
    @Nonnull
    @Override
    SparseVector score(@Nonnull UserHistory<? extends Event> profile, @Nonnull Collection<Long> items);

    /**
     * {@inheritDoc}
     * @deprecated Use {@link #predict(UserHistory, MutableSparseVector)}
     */
    @Deprecated
    @Override
    void score(@Nonnull UserHistory<? extends Event> profile, @Nonnull MutableSparseVector scores);
}
