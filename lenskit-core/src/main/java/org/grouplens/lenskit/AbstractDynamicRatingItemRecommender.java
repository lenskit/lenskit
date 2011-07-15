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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.grouplens.lenskit.data.ScoredLongList;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.vector.UserRatingVector;
import org.grouplens.lenskit.util.CollectionUtils;

/**
 * Base class for rating-based dynamic item recommenders. It implements all
 * methods required by {@link DynamicRatingItemRecommender} by delegating them
 * to a single method with a Fastutil-based interface.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public abstract class AbstractDynamicRatingItemRecommender extends AbstractItemRecommender implements DynamicRatingItemRecommender {

	protected AbstractDynamicRatingItemRecommender(DataAccessObject dao) {
		super(dao);
	}

	@Override
	public ScoredLongList recommend(UserRatingVector ratings) {
		return recommend(ratings, -1, null, ratings.keySet());
	}

	@Override
	public ScoredLongList recommend(UserRatingVector ratings, int n) {
		return recommend(ratings, n, null, ratings.keySet());
	}

	@Override
	public ScoredLongList recommend(UserRatingVector ratings, Set<Long> candidates) {
		return recommend(ratings, -1, CollectionUtils.fastSet(candidates), ratings.keySet());
	}

	@Override
	public ScoredLongList recommend(UserRatingVector ratings, int n,
			Set<Long> candidates, Set<Long> exclude) {
		LongSet cs = CollectionUtils.fastSet(candidates);
		LongSet es = CollectionUtils.fastSet(exclude);
		if (es == null)
			es = LongSets.EMPTY_SET;
		return recommend(ratings, n, cs, es);
	}

	@Override
	public ScoredLongList recommend(long user, int n, LongSet candidates, LongSet exclude) {
		return recommend(getRatings(user), n, candidates, exclude);
	}
	
	/**
	 * Implementation method for recommender services.
	 * @param ratings The user rating vector.
	 * @param n The number of items to return, or negative to return all possible
	 * items.
	 * @param candidates The candidate set.
	 * @param exclude The set of excluded items (the public methods convert
	 * null sets to the empty set, so this parameter is always non-null).
	 * @return The recommendations with associated scores.
	 * @see DynamicRatingItemRecommender#recommend(UserRatingVector, int, Set, Set)
	 */
	protected abstract ScoredLongList recommend(UserRatingVector ratings, int n,
			@Nullable LongSet candidates, @Nonnull LongSet exclude);
}