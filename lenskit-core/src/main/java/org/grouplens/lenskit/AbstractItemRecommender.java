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
import it.unimi.dsi.fastutil.longs.LongSets;

import java.util.Set;

import org.grouplens.lenskit.data.ScoredLongList;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.UserRatingVector;
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

	protected final RatingDataAccessObject dao;
	
	protected AbstractItemRecommender(RatingDataAccessObject dao) {
		this.dao = dao;
	}
	
	protected UserRatingVector getRatings(long user) {
		return UserRatingVector.fromRatings(user, dao.getUserRatings(user));
	}
	
	@Override
	public ScoredLongList recommend(long user) {
		return recommend(user, -1, null, getRatings(user).keySet());
	}

	@Override
	public ScoredLongList recommend(long user, int n) {
		return recommend(user, n, null, getRatings(user).keySet());
	}

	@Override
	public ScoredLongList recommend(long user, Set<Long> candidates) {
		return recommend(user, -1, candidates, getRatings(user).keySet());
	}

	@Override
	public ScoredLongList recommend(long user, int n, Set<Long> candidates,
			Set<Long> exclude) {
		LongSet cs = CollectionUtils.fastSet(candidates);
		LongSet es = CollectionUtils.fastSet(exclude);
		if (es == null)
			es = LongSets.EMPTY_SET;
		return recommend(user, n, cs, es);
	}
	
	/**
	 * Implementation method for recommender services.
	 * @param user The user ID.
	 * @param n The number of items to return, or negative to return all possible
	 * items.
	 * @param candidates The candidate set.
	 * @param exclude The set of excluded items (the public methods convert
	 * null sets to the empty set, so this parameter is always non-null).
     * @return A list of <tt>ScoredId</tt> objects representing recommended items.
	 * @see ItemRecommender#recommend(long, int, Set, Set)
	 */
	protected abstract ScoredLongList recommend(long user, int n, LongSet candidates, LongSet exclude);
}