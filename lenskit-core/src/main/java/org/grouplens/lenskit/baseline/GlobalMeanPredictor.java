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

import javax.inject.Inject;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;

/**
 * Rating scorer that predicts the global mean rating for all items.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
@DefaultProvider(GlobalMeanPredictor.Provider.class)
@Shareable
public class GlobalMeanPredictor extends ConstantPredictor {
    /**
     * A default builder used to create GlobalMeanPredictors.
     *
     * @author Michael Ludwig <mludwig@cs.umn.edu>
     */
    public static class Provider implements javax.inject.Provider<GlobalMeanPredictor> {
        private DataAccessObject dao;
        
        @Inject
        public Provider(@Transient DataAccessObject dao) {
            this.dao = dao;
        }
        
        @Override
        public GlobalMeanPredictor get() {
            double avg = computeMeanRating(dao);
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
     * @return The average of the rating values stored in <var>ratings</var>.
     */
    public static double computeMeanRating(DataAccessObject dao) {
        double total = 0;
        long count = 0;

        Cursor<Rating> ratings = dao.getEvents(Rating.class);
        for (Rating r: ratings.fast()) {
            if (r.getPreference() != null) {
                total += r.getPreference().getValue();
                count += 1;
            }
        }
        ratings.close();

        double avg = 0;
        if (count > 0)
            avg = total / count;

        return avg;
    }
}
