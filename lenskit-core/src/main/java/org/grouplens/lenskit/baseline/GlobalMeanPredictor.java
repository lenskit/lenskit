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
package org.grouplens.lenskit.baseline;

import javax.annotation.WillClose;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;

/**
 * Rating predictor that predicts the global mean rating for all items.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class GlobalMeanPredictor extends ConstantPredictor {
    private static final long serialVersionUID = 1L;

    /**
     * Construct a new global mean predictor from a data source.
     * @param ratings A data source of ratings.
     */
    public GlobalMeanPredictor(RatingDataAccessObject ratings) {
        super(computeMeanRating(ratings.getRatings()));
    }

    /**
     * Helper method to compute the mean of all ratings in a cursor.
     * The cursor is closed after the ratings are computed.
     * @param ratings A cursor of ratings to average.
     * @return The arithmetic mean of all ratings.
     */
    public static double computeMeanRating(@WillClose Cursor<Rating> ratings) {
        double total = 0;
        long count = 0;
        try {
            for (Rating r: ratings) {
                total += r.getRating();
                count += 1;
            }
        } finally {
            ratings.close();
        }
        double avg = 0;
        if (count > 0)
            avg = total / count;
        return avg;
    }
}
