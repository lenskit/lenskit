package org.grouplens.lenskit.core;

import it.unimi.dsi.fastutil.longs.LongLists;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collection;
import java.util.HashSet;
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
    public ScoredLongList globalRecommend(long item) {
    	return globalRecommend(item, -1, null, null);
    }

    /**
     * Delegate to {@link #globalRecommend(LongSet, int, LongSet, LongSet)}.
     */
    @Override 
    public ScoredLongList globalRecommend(long item, int n) {
    	return globalRecommend(item, n, null, null);
    }
    
    /**
     * Delegate to {@link #globalRecommend(LongSet, int, LongSet, LongSet)}.
     */
    @Override 
    public ScoredLongList globalRecommend(long item, Set<Long> candidates) {
    	return globalRecommend(item, -1, candidates, null);
    }
    
    /**
     * Delegate to {@link #globalRecommend(LongSet, int, LongSet, LongSet)}.
     */
    @Override 
    public ScoredLongList globalRecommend(long item, int n, Set<Long> candidates, Set<Long> exclude) {
        Set<Long> it = new HashSet<Long>();
        it.add(item);
    	LongSet cs = CollectionUtils.fastSet(candidates);
        LongSet es = CollectionUtils.fastSet(exclude);
        return globalRecommend(it, n, cs, es);

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
