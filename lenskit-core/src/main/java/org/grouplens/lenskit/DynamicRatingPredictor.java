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

import javax.annotation.Nonnull;

import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.data.vector.UserRatingVector;

/**
 * Interface for predicting ratings from user rating histories.  This interface
 * is like {@link RatingPredictor}, except the client passes in the user's
 * ratings to be used in computation.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface DynamicRatingPredictor {
    /**
     * Generate predictions for a collection of items.
     * 
     * @param profile The user's profile
     * @param items the items for which predictions are desired
     * @return A mapping from item IDs to predicted preference. This mapping may
     *         not contain all requested items.
     */
    @Nonnull
    public SparseVector predict(UserHistory<? extends Event> profile, Collection<Long> items);
    
    /**
     * Generate predictions for a collection of items.
     * 
     * @param ratings The user rating vector
     * @param items the items for which predictions are desired
     * @return A mapping from item IDs to predicted preference. This mapping may
     *         not contain all requested items.
     */
    @Nonnull
    public SparseVector predict(UserRatingVector ratings, Collection<Long> items);
}
