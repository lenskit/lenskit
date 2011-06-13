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

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.CollectionUtils;

/**
 * Base class for rating recommenders.  It implements all methods required by
 * {@link DynamicRatingItemRecommender} by delegating them to a single method with a
 * Fastutil-based interface.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractRatingRecommender implements ItemRecommender, DynamicRatingItemRecommender {
    protected final RatingDataAccessObject dao;
    
    public AbstractRatingRecommender(RatingDataAccessObject dao) {
        this.dao = dao;
    }
    
    protected SparseVector getRatings(long user) {
        return Ratings.userRatingVector(dao.getUserRatings(user));
    }
    
    public List<ScoredId> recommend(long user) {
        return recommend(user, getRatings(user));
    }
    
    public List<ScoredId> recommend(long user, int n) {
        return recommend(user, getRatings(user), n);
    }
    
    public List<ScoredId> recommend(long user, @Nullable Set<Long> candidates) {
        return recommend(user, getRatings(user), candidates);
    }
    
    public List<ScoredId> recommend(long user, int n, @Nullable Set<Long> candidates,
        @Nullable Set<Long> exclude) {
        return recommend(user, getRatings(user), n, candidates, exclude);
    }
    
    /**
     * Implementation method for recommender services.
     * @param user The user ID.
     * @param ratings The user's rating vector.
     * @param n The number of items to return, or negative to return all possible
     * items.
     * @param candidates The candidate set.
     * @param exclude The set of excluded items (the public methods convert
     * null sets to the empty set, so this parameter is always non-null).
     * @return A list of <tt>ScoredId</tt> objects representing recommended items.
     * @see DynamicRatingItemRecommender#recommend(long, SparseVector, int, Set, Set)
     */
    protected abstract List<ScoredId> recommend(long user, SparseVector ratings, int n,
            @Nullable LongSet candidates, @Nonnull LongSet exclude);

    @Override
    public List<ScoredId> recommend(long user, SparseVector ratings) {
        return recommend(user, ratings, -1, null, ratings.keySet());
    }

    @Override
    public List<ScoredId> recommend(long user, SparseVector ratings, int n) {
        return recommend(user, ratings, n, null, ratings.keySet());
    }

    @Override
    public List<ScoredId> recommend(long user, SparseVector ratings,
            Set<Long> candidates) {
        return recommend(user, ratings, -1,
                CollectionUtils.fastSet(candidates), ratings.keySet());
    }

    @Override
    public List<ScoredId> recommend(long user, SparseVector ratings, int n,
            Set<Long> candidates, Set<Long> exclude) {
        LongSet cs = CollectionUtils.fastSet(candidates);
        LongSet es = CollectionUtils.fastSet(exclude);
        if (es == null)
            es = LongSets.EMPTY_SET;
        return recommend(user, ratings, n, cs, es);
    }

}
