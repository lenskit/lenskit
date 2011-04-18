package org.grouplens.lenskit;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collection;

import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.LongSortedArraySet;

/**
 * Base class implementing {@link RatingPredictor} in terms of 
 * {@link DynamicRatingPredictor}.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractDynamicRatingPredictor implements RatingPredictor, DynamicRatingPredictor {
    private RatingDataAccessObject dao;
    
    protected AbstractDynamicRatingPredictor(RatingDataAccessObject dao) {
        this.dao = dao;
    }
    
    protected RatingDataAccessObject getDAO() {
        return dao;
    }
    
    /**
     * Delegate to {@link #predict(long, java.util.Collection)}.
     */
    public ScoredId predict(long user, long item) {
        LongSet items = new LongSortedArraySet(new long[]{item});
        SparseVector p = predict(user, items);
        double pred = p.get(item);
        if (Double.isNaN(pred))
            return null;
        else
            return new ScoredId(item, pred);
    }
    
    /**
     * Delegate to {@link #predict(long, java.util.Collection, java.util.Collection)}
     */
    public SparseVector predict(long user, Collection<Long> items) {
        Collection<Rating> ratings = Cursors.makeList(getDAO().getUserRatings(user));
        return predict(user, ratings, items);
    }
    
    /**
     * Delegate to {@link #predict(long, SparseVector, Collection)}.
     */
    public SparseVector predict(long user, Collection<Rating> ratings, Collection<Long> items) {
        return predict(user, Ratings.userRatingVector(ratings), items);
    }
}
