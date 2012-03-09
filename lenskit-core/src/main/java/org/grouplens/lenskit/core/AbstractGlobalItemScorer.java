package org.grouplens.lenskit.core;

import it.unimi.dsi.fastutil.longs.LongLists;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.GlobalItemScorer;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Base class to make global item scorers easier to implement. Delegates single=item
 * score methods to collection-based ones, 
 *
 * @author Steven Chang <schang@cs.umn.edu>		   
 *
 */
public abstract class AbstractGlobalItemScorer implements GlobalItemScorer{
    /**
     * The DAO passed to the constructor.
     */
    protected final @Nonnull DataAccessObject dao;

    /**
     * Initialize the abstract item scorer.
     * 
     * @param dao The data access object to use for retrieving histories.
     */
    protected AbstractGlobalItemScorer(@Nonnull DataAccessObject dao) {
        this.dao = dao;
    }
    
    
    /**
     * Delegate to {@link #globalScore(Collection, Collection)}
     */
    @Override
	public double globalScore(Collection<Long> queryItems, long item){
    	SparseVector v = globalScore(queryItems, LongLists.singleton(item));
		return v.get(item, Double.NaN);
    }



}
