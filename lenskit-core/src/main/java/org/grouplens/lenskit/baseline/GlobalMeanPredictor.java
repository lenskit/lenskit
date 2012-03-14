/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.core.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.data.snapshot.RatingSnapshot;
import org.grouplens.lenskit.params.meta.Built;

/**
 * Rating scorer that predicts the global mean rating for all items.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
@Built
public class GlobalMeanPredictor extends ConstantPredictor {
    /**
     * A default builder used to create GlobalMeanPredictors.
     *
     * @author Michael Ludwig <mludwig@cs.umn.edu>
     */
    public static class Builder extends RecommenderComponentBuilder<GlobalMeanPredictor> {
        @Override
        public GlobalMeanPredictor build() {
            double avg = computeMeanRating(snapshot);
            return new GlobalMeanPredictor(avg);
        }
    }

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new global mean scorer where it is assumed
     * that the given value is the global mean.
     * @param mean
     */
    public GlobalMeanPredictor(double mean) {
        super(mean);
    }

    /**
     * Utility method to compute the mean or average of the rating values
     * contained in the given collection of ratings.
     *
     * @param ratings
     * @return The average of the rating values stored in <var>ratings</var>.
     */
    public static double computeMeanRating(RatingSnapshot ratings) {
        double total = 0;
        long count = 0;

        for (Preference r: ratings.getRatings().fast()) {
            total += r.getValue();
            count += 1;
        }

        double avg = 0;
        if (count > 0)
            avg = total / count;

        return avg;
    }
}
