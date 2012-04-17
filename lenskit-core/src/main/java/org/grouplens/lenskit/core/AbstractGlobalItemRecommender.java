/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.GlobalItemRecommender;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.data.dao.DataAccessObject;


/**
 * Base class for item recommenders. It implements all methods required by
 * {@link GlobalItemRecommender} by delegating them to general methods with
 * fastutil-based interfaces.
 * 
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public abstract class AbstractGlobalItemRecommender implements GlobalItemRecommender{
    protected final DataAccessObject dao;

    protected AbstractGlobalItemRecommender(DataAccessObject dao) {
        this.dao = dao;
    }
    
    /**
     * Delegate to {@link #globalRecommend(LongSet, int, LongSet, LongSet)}.
     */
    @Override
	public ScoredLongList globalRecommend(Set<Long> items){
    	return globalRecommend(items, -1, null, null);
    }

    /**
     * Delegate to {@link #globalRecommend(LongSet, int, LongSet, LongSet)}.
     */
    @Override 
    public ScoredLongList globalRecommend(Set<Long> items, int n){
    	return globalRecommend(items, n, null, null);
    } 

    /**
     * Delegate to {@link #globalRecommend(LongSet, int, LongSet, LongSet)}.
     */
    @Override 
    public ScoredLongList globalRecommend(Set<Long> items, @Nullable Set<Long> candidates){
    	return globalRecommend(items, -1, candidates, null);
    }
    
    /**
     * Delegate to {@link #globalRecommend(LongSet, int, LongSet, LongSet)}.
     */
    @Override 
    public ScoredLongList globalRecommend(Set<Long> items, int n, @Nullable Set<Long> candidates,
                             @Nullable Set<Long> exclude){
    	LongSet it = CollectionUtils.fastSet(items);
    	LongSet cs = CollectionUtils.fastSet(candidates);
        LongSet es = CollectionUtils.fastSet(exclude);
        return globalRecommend(it, n, cs, es);
    }
    
    
    
    /**
     * Implementation method for global item recommendation.  
     *
     * @param items The items ID.
     * @param n The number of items to return, or negative to return all
     *        possible items.
     * @param candidates The candidate set.
     * @param exclude The set of excluded items, or <tt>null</tt> to use the
     *        default exclude set.
     * @return A list of <tt>ScoredId</tt> objects representing recommended
     *         items.
     * @see GlobalItemRecommender#globalRecommend(long, int, Set, Set)
     */
    protected abstract ScoredLongList globalRecommend(LongSet items, int n, 
    												  @Nullable LongSet candidates,
    												  @Nullable LongSet exclude);

    
}
