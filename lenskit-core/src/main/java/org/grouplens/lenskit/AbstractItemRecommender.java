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
import org.grouplens.lenskit.util.CollectionUtils;


/**
 * Base class for item recommenders.  It implements all methods required by
 * {@link ItemRecommender} by delegating them to a single method with a
 * Fastutil-based interface.
 * 
 * @fixme This should not use the rating DAO. That will change in a future
 * LensKit release.
 */
public abstract class AbstractItemRecommender implements ItemRecommender {
    @Override
	public ScoredLongList recommend(long user) {
		return recommend(user, -1, null, null);
	}

	@Override
	public ScoredLongList recommend(long user, int n) {
		return recommend(user, n, null, null);
	}

	@Override
	public ScoredLongList recommend(long user, Set<Long> candidates) {
		return recommend(user, -1, candidates, null);
	}

	@Override
	public ScoredLongList recommend(long user, int n, Set<Long> candidates,
			Set<Long> exclude) {
		LongSet cs = CollectionUtils.fastSet(candidates);
		LongSet es = CollectionUtils.fastSet(exclude);
		return recommend(user, n, cs, es);
	}
	
	    /**
     * Implementation method for recommender services.
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
	protected abstract ScoredLongList recommend(long user, int n, 
	                                            @Nullable LongSet candidates,
	                                            @Nullable LongSet exclude);
}