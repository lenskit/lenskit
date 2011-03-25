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
package org.grouplens.lenskit.eval.crossfold;

import java.util.ArrayList;
import java.util.Collection;

import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * User rating profile splitter based on re-ordering.  This splitter uses
 * {@link #orderRatings(Collection)} to re-order the ratings, then holds out the
 * last ratings.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class OrderedFractionalUserRatingProfileSplitter implements
        UserRatingProfileSplitter {
    private final double holdoutFraction;
    
    /**
     * Cosntruct a new splitter.
     * @param fraction The fraction of ratings to hold out.
     */
    public OrderedFractionalUserRatingProfileSplitter(double fraction) {
        holdoutFraction = fraction;
    }
    
    /**
     * Re-order the ratings.  The last <var>holdoutFraction</var> ratings will
     * be the probe, and the remaining ratings used as the query set. 
     * @param ratings The rating collection.
     */
    protected abstract void orderRatings(ArrayList<Rating> ratings);

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.eval.crossfold.UserRatingProfileSplitter#splitProfile(org.grouplens.lenskit.data.UserRatingProfile)
     */
    @Override
    public SplitUserRatingProfile splitProfile(UserRatingProfile profile) {
        ArrayList<Rating> ratings = new ArrayList<Rating>(profile.getRatings());
        // Compute the split point - everything before it is train.
        final int midpt = (int) Math.round(ratings.size() * (1.0 - holdoutFraction));
        orderRatings(ratings);
        
        // Extract query and probe sets
        final SparseVector queryRatings =
            Ratings.userRatingVector(ratings.subList(0, midpt));
        final SparseVector probeRatings =
            Ratings.userRatingVector(ratings.subList(midpt, ratings.size()));
        assert queryRatings.size() + probeRatings.size() == ratings.size();
        
        return new SplitUserRatingProfile(profile.getUser(), queryRatings, probeRatings);
    }

}
