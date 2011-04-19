package org.grouplens.lenskit;

import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.LongSortedArraySet;

public abstract class AbstractRatingPredictor implements RatingPredictor {

    private RatingDataAccessObject dao;

    public AbstractRatingPredictor(RatingDataAccessObject dao) {
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

}