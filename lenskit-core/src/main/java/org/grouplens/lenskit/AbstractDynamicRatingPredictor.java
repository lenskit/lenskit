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


import java.util.Collection;

import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Base class implementing {@link RatingPredictor} in terms of 
 * {@link DynamicRatingPredictor}.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractDynamicRatingPredictor extends AbstractRatingPredictor implements DynamicRatingPredictor {
    protected AbstractDynamicRatingPredictor(RatingDataAccessObject dao) {
        super(dao);
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
