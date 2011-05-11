package org.grouplens.lenskit.data.context;

import javax.annotation.concurrent.ThreadSafe;

import org.grouplens.lenskit.Builder;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;

@ThreadSafe
public abstract class BuildContextBuilder<T extends RatingBuildContext> implements Builder<T> {
    protected RatingDataAccessObject dao;
    
    public void setRatingDataAccessObject(RatingDataAccessObject dao) {
        this.dao = dao;
    }
}
