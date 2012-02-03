/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.lenskit.core;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Set;

import javax.annotation.Nullable;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;


/**
 * Base class for item recommenders. It implements all methods required by
 * {@link ItemRecommender} by delegating them to general methods with
 * fastutil-based interfaces.
 */
public abstract class AbstractItemRecommender implements ItemRecommender {
    protected final DataAccessObject dao;

    protected AbstractItemRecommender(DataAccessObject dao) {
        this.dao = dao;
    }

    /**
     * Delegate to {@link #recommend(long, int, LongSet, LongSet)}.
     */
    @Override
    public ScoredLongList recommend(long user) {
        return recommend(user, -1, null, null);
    }

    /**
     * Delegate to {@link #recommend(long, int, LongSet, LongSet)}.
     */
    @Override
    public ScoredLongList recommend(long user, int n) {
        return recommend(user, n, null, null);
    }

    /**
     * Delegate to {@link #recommend(long, int, LongSet, LongSet)}.
     */
    @Override
    public ScoredLongList recommend(long user, Set<Long> candidates) {
        return recommend(user, -1, candidates, null);
    }

    /**
     * Delegate to {@link #recommend(long, int, LongSet, LongSet)}.
     */
    @Override
    public ScoredLongList recommend(long user, int n, Set<Long> candidates,
                                    Set<Long> exclude) {
        LongSet cs = CollectionUtils.fastSet(candidates);
        LongSet es = CollectionUtils.fastSet(exclude);
        return recommend(user, n, cs, es);
    }

    /**
     * Delegate to {@link #recommend(UserHistory, int, LongSet, LongSet)}.
     */
    @Override
    public ScoredLongList recommend(UserHistory<? extends Event> ratings) {
        return recommend(ratings, -1, null, null);
    }

    /**
     * Delegate to {@link #recommend(UserHistory, int, LongSet, LongSet)}.
     */
    @Override
    public ScoredLongList recommend(UserHistory<? extends Event> ratings, int n) {
        return recommend(ratings, n, null, null);
    }

    /**
     * Delegate to {@link #recommend(UserHistory, int, LongSet, LongSet)}.
     */
    @Override
    public ScoredLongList recommend(UserHistory<? extends Event> ratings, Set<Long> candidates) {
        return recommend(ratings, -1, CollectionUtils.fastSet(candidates), null);
    }

    /**
     * Delegate to {@link #recommend(UserHistory, int, LongSet, LongSet)}.
     */
    @Override
    public ScoredLongList recommend(UserHistory<? extends Event> ratings, int n,
            Set<Long> candidates, Set<Long> exclude) {
        LongSet cs = CollectionUtils.fastSet(candidates);
        LongSet es = CollectionUtils.fastSet(exclude);
        return recommend(ratings, n, cs, es);
    }


    /**
     * Return <tt>true</tt>, indicating this recommender can use histories.
     * Override this if the recommender actually doesn't.
     */
    @Override
    public boolean canUseHistory() {
        return true;
    }

    /**
     * Implementation method for ID-based recommendation.  All other ID-based
     * methods are implemented in terms of this one, which in turn delegates to
     * {@link #recommend(UserHistory, int, LongSet, LongSet)} with a history
     * obtained by {@link #getUserHistory(long)}.
     *
     * @param user The user ID.
     * @param n The number of items to return, or negative to return all
     *        possible items.
     * @param candidates The candidate set.
     * @param exclude The set of excluded items, or <tt>null</tt> to use the
     *        default exclude set.
     * @return A list of <tt>ScoredId</tt> objects representing recommended
     *         items.
     * @see ItemRecommender#recommend(long, int, Set, Set)
     */
    protected ScoredLongList recommend(long user, int n, LongSet candidates, LongSet exclude) {
        return recommend(getUserHistory(user), n, candidates, exclude);
    }

    /**
     * Get the history for the specified user. If a subclass can only take
     * advantage of particular elements, it can override this method to filter
     * based on them.
     *
     * @param user The ID of the user whose history is requested.
     * @return The history for the specified user.
     */
    protected UserHistory<? extends Event> getUserHistory(long user) {
        return dao.getUserHistory(user);
    }

    /**
     * Implementation method for recommender services.
     *
     * @param ratings The user rating vector.
     * @param n The number of items to return, or negative to return all
     *        possible items.
     * @param candidates The candidate set.
     * @param exclude The set of excluded items, or <tt>null</tt> for the
     *        default exclude set.
     * @return The recommendations with associated scores.
     * @see ItemRecommender#recommend(UserHistory, int, Set, Set)
     */
    protected abstract ScoredLongList recommend(UserHistory<? extends Event> ratings, int n,
                                                @Nullable LongSet candidates,
                                                @Nullable LongSet exclude);
    

    
}