/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.Collection;

import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.vector.SparseVector;

/**
 * Base class to make item scorers easier to implement. Delegates single=item
 * score methods to collection-based ones, and {@link #score(long, Collection)}
 * to {@link #score(UserHistory, Collection)}.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractItemScorer implements RatingPredictor {
    protected final DataAccessObject dao;

    protected AbstractItemScorer(DataAccessObject dao) {
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
     * Delegate to {@link #score(UserHistory, Collection)}.
     */
    @Override
    public SparseVector score(long user, Collection<Long> items) {
        UserHistory<? extends Event> profile = getUserHistory(user);
        return score(profile, items);
    }

    /**
     * Delegate to {@link #score(long, Collection)}.
     */
    @Override
    public double score(long user, long item) {
        LongList l = new LongArrayList(1);
        l.add(item);
        SparseVector v = score(user, l);
        return v.get(item, Double.NaN);
    }

    /**
     * Default implementation can use history.  Override this ins ubclasses that
     * don't.
     */
    @Override
    public boolean canUseHistory() {
        return true;
    }

    /**
     * Delegate to {@link #score(UserHistory, Collection)}
     */
    @Override
    public double score(UserHistory<? extends Event> profile, long item) {
        LongList l = new LongArrayList(1);
        l.add(item);
        SparseVector v = score(profile, l);
        return v.get(item, Double.NaN);
    }
}