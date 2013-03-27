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
package org.grouplens.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongLists;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Base class to make item scorers easier to implement. Delegates single-item
 * score methods to collection-based ones, and {@link #score(long, MutableSparseVector)}
 * to {@link #score(UserHistory, MutableSparseVector)}.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public abstract class AbstractItemScorer implements ItemScorer {
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
    protected AbstractItemScorer(@Nonnull DataAccessObject dao) {
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
     * <p>Delegates to {@link #score(long, MutableSparseVector)}.
     */
    @Nonnull
    @Override
    public SparseVector score(long user, @Nonnull Collection<Long> items) {
        MutableSparseVector scores = new MutableSparseVector(items);
        score(user, scores);
        // FIXME Create a more efficient way of "releasing" mutable sparse vectors
        return scores.freeze();
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #score(UserHistory, MutableSparseVector)}.
     */
    @Nonnull
    @Override
    public SparseVector score(@Nonnull UserHistory<? extends Event> history,
                              @Nonnull Collection<Long> items) {
        MutableSparseVector scores = new MutableSparseVector(items);
        score(history, scores);
        return scores.freeze();
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #score(UserHistory, MutableSparseVector)}, with a
     * history retrieved from the DAO.
     *
     * @param user   The user ID.
     * @param scores The score vector.
     * @see #getUserHistory(long)
     */
    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        UserHistory<? extends Event> profile = getUserHistory(user);
        score(profile, scores);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #score(long, Collection)}.
     */
    @Override
    public double score(long user, long item) {
        SparseVector v = score(user, LongLists.singleton(item));
        return v.get(item, Double.NaN);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #score(UserHistory, Collection)}.
     */
    @Override
    public double score(@Nonnull UserHistory<? extends Event> profile, long item) {
        SparseVector v = score(profile, LongLists.singleton(item));
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
}
