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

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Set;

import javax.annotation.Nullable;

import org.grouplens.lenskit.data.ScoredLongList;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.util.CollectionUtils;

/**
 * Base class for rating-based dynamic item recommenders. It implements all
 * methods required by {@link DynamicItemRecommender} and
 * {@link #recommend(long, int, Set, Set)} by delegating them to a single method
 * {@link #recommend(UserHistory, int, LongSet, LongSet)}
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public abstract class AbstractDynamicItemRecommender extends AbstractItemRecommender implements DynamicItemRecommender {

    protected final DataAccessObject dao;
    
	protected AbstractDynamicItemRecommender(DataAccessObject dao) {
	    this.dao = dao;
	}

	@Override
	public ScoredLongList recommend(UserHistory<? extends Event> ratings) {
		return recommend(ratings, -1, null, null);
	}

	@Override
	public ScoredLongList recommend(UserHistory<? extends Event> ratings, int n) {
		return recommend(ratings, n, null, null);
	}

	@Override
	public ScoredLongList recommend(UserHistory<? extends Event> ratings, Set<Long> candidates) {
		return recommend(ratings, -1, CollectionUtils.fastSet(candidates), null);
	}

	@Override
	public ScoredLongList recommend(UserHistory<? extends Event> ratings, int n,
			Set<Long> candidates, Set<Long> exclude) {
		LongSet cs = CollectionUtils.fastSet(candidates);
		LongSet es = CollectionUtils.fastSet(exclude);
		return recommend(ratings, n, cs, es);
	}

	@Override
	public ScoredLongList recommend(long user, int n, LongSet candidates, LongSet exclude) {
		return recommend(getUserHistory(user), n, candidates, exclude);
	}
	
	/**
	 * Get the history for the specified user.
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
     * @see DynamicItemRecommender#recommend(UserHistory, int, Set, Set)
     */
	protected abstract ScoredLongList recommend(UserHistory<? extends Event> ratings, int n,
	                                            @Nullable LongSet candidates,
	                                            @Nullable LongSet exclude);
}