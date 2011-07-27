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

import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.data.vector.UserRatingVector;

/**
 * Base class for dynamic rating predictors.  Implementers only need to write
 * a {@link #predict(UserRatingVector, Collection)} method to have both a
 * {@link RatingPredictor} and a {@link DynamicRatingPredictor}.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public abstract class AbstractDynamicRatingPredictor extends AbstractRatingPredictor implements DynamicRatingPredictor {
    protected final DataAccessObject dao;

    protected AbstractDynamicRatingPredictor(DataAccessObject dao) {
        this.dao = dao;
    }
    
    protected UserHistory<Rating> getUserRatings(long user) {
        return dao.getUserHistory(user, Rating.class);
    }
    
    /**
     * Delegate to {@link #predict(UserHistory, Collection)}.
     */
    @Override
    public SparseVector predict(long user, Collection<Long> items) {
        UserHistory<Rating> ratings = getUserRatings(user);
        return predict(ratings, items);
    }
    
    /**
     * Delegate to {@link #predict(UserRatingVector, Collection)}.
     */
    @Override
    public SparseVector predict(UserHistory<? extends Event> profile, Collection<Long> items) {
        return predict(UserRatingVector.fromRatings(profile.filter(Rating.class)), items);
    }
}
