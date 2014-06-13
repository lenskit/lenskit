/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
import javax.inject.Provider;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;

/**
 * Rating scorer that predicts the global mean rating for all items.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(GlobalMeanRatingItemScorer.Builder.class)
@Shareable
public class GlobalMeanRatingItemScorer extends ConstantItemScorer {
    /**
     * A default builder used to create GlobalMeanPredictors.
     *
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder implements Provider<GlobalMeanRatingItemScorer> {
        private EventDAO dao;

        /**
         * Construct a new provider.
         *
         * @param dao The DAO.
         */
        @Inject
        public Builder(@Transient EventDAO dao) {
            this.dao = dao;
        }

        @Override
        public GlobalMeanRatingItemScorer get() {
            double avg = computeMeanRating(dao);
            return new GlobalMeanRatingItemScorer(avg);
        }
    }

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new global mean scorer where it is assumed
     * that the given value is the global mean.
     *
     * @param mean The global mean.
     */
    public GlobalMeanRatingItemScorer(double mean) {
        super(mean);
    }

    /**
     * Utility method to compute the mean or average of the rating values
     * contained in the given collection of ratings.
     *
     * @param dao The DAO to average.
     * @return The average of the rating values stored in {@var ratings}.
     */
    public static double computeMeanRating(EventDAO dao) {
        double total = 0;
        long count = 0;

        Cursor<Rating> ratings = dao.streamEvents(Rating.class);
        try {
            for (Rating r : ratings.fast()) {
                Preference p = r.getPreference();
                if (p != null) {
                    total += p.getValue();
                    count += 1;
                }
            }
        } finally {
            ratings.close();
        }

        double avg = 0;
        if (count > 0) {
            avg = total / count;
        }

        return avg;
    }
}
