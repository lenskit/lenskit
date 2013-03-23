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
package org.grouplens.lenskit.core;

import it.unimi.dsi.fastutil.longs.LongLists;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Base class to make rating predictors easier to implement. Delegates single-item predict methods
 * to collection-based ones, and {@link #predict(long, MutableSparseVector)} to {@link
 * #predict(org.grouplens.lenskit.data.UserHistory, MutableSparseVector)}. It also delegates all
 * deprecated {@code score} methods to their corresponding {@code predict} methods.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public abstract class AbstractRatingPredictor implements RatingPredictor {
    /**
     * The DAO passed to the constructor.
     */
    @Nonnull
    protected final DataAccessObject dao;

    /**
     * Initialize the abstract item scorer.
     *
     * @param dao The data access object to use for retrieving histories.
     */
    protected AbstractRatingPredictor(@Nonnull DataAccessObject dao) {
        this.dao = dao;
    }

    /**
     * Get the user's history. Subclasses that only require a particular type of
     * event can override this to filter the history.
     *
     * @param user The user whose history is required.
     * @return The event history for this user.
     */
    protected UserHistory<? extends Event> getUserHistory(long user) {
        return dao.getUserHistory(user);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #predict(long, MutableSparseVector)}.
     */
    @Nonnull
    @Override
    public SparseVector predict(long user, @Nonnull Collection<Long> items) {
        MutableSparseVector scores = new MutableSparseVector(items);
        predict(user, scores);
        // FIXME Create a more efficient way of "releasing" mutable sparse vectors
        return scores.freeze();
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #predict(org.grouplens.lenskit.data.UserHistory, MutableSparseVector)}.
     */
    @Nonnull
    @Override
    public SparseVector predict(@Nonnull UserHistory<? extends Event> history,
                                @Nonnull Collection<Long> items) {
        MutableSparseVector scores = new MutableSparseVector(items);
        predict(history, scores);
        return scores.freeze();
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #predict(org.grouplens.lenskit.data.UserHistory, MutableSparseVector)}, with a
     * history retrieved from the DAO.
     *
     * @param user   The user ID.
     * @param scores The score vector.
     * @see #getUserHistory(long)
     */
    @Override
    public void predict(long user, @Nonnull MutableSparseVector scores) {
        UserHistory<? extends Event> profile = getUserHistory(user);
        predict(profile, scores);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #predict(long, java.util.Collection)}.
     */
    @Override
    public double predict(long user, long item) {
        SparseVector v = predict(user, LongLists.singleton(item));
        return v.get(item, Double.NaN);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #predict(org.grouplens.lenskit.data.UserHistory, java.util.Collection)}.
     */
    @Override
    public double predict(@Nonnull UserHistory<? extends Event> profile, long item) {
        SparseVector v = predict(profile, LongLists.singleton(item));
        return v.get(item, Double.NaN);
    }

    /**
     * {@inheritDoc}
     * <p>Default implementation assumes history is usable. Override this in subclasses where
     * it isn't.
     */
    @Override
    public boolean canUseHistory() {
        return true;
    }

    @Deprecated
    @Override
    public double score(long user, long item) {
        return predict(user, item);
    }

    @Deprecated
    @Nonnull
    @Override
    public SparseVector score(long user, @Nonnull Collection<Long> items) {
        return predict(user, items);
    }

    @Deprecated
    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        predict(user, scores);
    }

    @Deprecated
    @Override
    public double score(@Nonnull UserHistory<? extends Event> profile, long item) {
        return predict(profile, item);
    }

    @Deprecated
    @Nonnull
    @Override
    public SparseVector score(@Nonnull UserHistory<? extends Event> profile, @Nonnull Collection<Long> items) {
        return predict(profile, items);
    }

    @Deprecated
    @Override
    public void score(@Nonnull UserHistory<? extends Event> profile, @Nonnull MutableSparseVector scores) {
        predict(profile, scores);
    }
}
