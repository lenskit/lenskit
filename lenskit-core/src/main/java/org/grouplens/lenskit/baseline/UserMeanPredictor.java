/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.util.IDMeanAccumulator;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

import static java.lang.Math.abs;

/**
 * Rating scorer that returns the user's average rating for all predictions.
 *
 * If the user has no ratings, the global mean is returned.  This is done by
 * actually computing the average offset from the global mean and adding back
 * the global mean for the returned prediction.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(UserMeanPredictor.Builder.class)
@Shareable
public class UserMeanPredictor extends AbstractBaselinePredictor {
    private static final Logger logger = LoggerFactory.getLogger(UserMeanPredictor.class);

    /**
     * A builder that creates UserMeanPredictors.
     *
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder implements Provider<UserMeanPredictor> {
        private double damping = 0;
        private DataAccessObject dao;

        /**
         * Create a new user mean predictor.
         *
         * @param dao     The DAO.
         * @param damping The damping term.
         */
        @Inject
        public Builder(@Transient DataAccessObject dao,
                       @MeanDamping double damping) {
            this.dao = dao;
            this.damping = damping;
        }

        @Override
        public UserMeanPredictor get() {
            logger.debug("Building new user mean scorer");

            logger.debug("damping = {}", damping);

            final ImmutableSparseVector userMeans;
            final double mean;
            Cursor<Rating> cur = dao.getEvents(Rating.class);
            try {
                IDMeanAccumulator accum = new IDMeanAccumulator();
                // TODO Make this work properly with multiple ratings
                for (Rating r: cur.fast()) {
                    Preference p = r.getPreference();
                    if (p != null) {
                        accum.put(p.getUserId(), p.getValue());
                    }
                }
                userMeans = accum.idMeanOffsets(damping);
                mean = accum.globalMean();
            } finally {
                cur.close();
            }

            logger.debug("Computed global mean {}", mean);
            return new UserMeanPredictor(mean, damping, userMeans);
        }
    }

    private static final long serialVersionUID = 2L;

    private final double globalMean;
    private final double damping;
    private final ImmutableSparseVector userMeans;

    /**
     * Construct a scorer that computes user means offset by the global mean.
     *
     * @param mean   The global mean rating.
     * @param damp   A damping term for the calculations.
     * @param umeans The mean rating for each user.
     */
    public UserMeanPredictor(double mean, double damp, ImmutableSparseVector umeans) {
        globalMean = mean;
        damping = damp;
        userMeans = umeans;
    }

    @Override
    public void predict(long user, MutableSparseVector output, boolean predictSet) {
        double mean = globalMean + userMeans.get(user, 0);
        if (predictSet) {
            output.fill(mean);
        } else {
            for (VectorEntry e : output.fast(VectorEntry.State.UNSET)) {
                output.set(e, mean);
            }
        }
    }

    static double average(SparseVector ratings, double globalMean, double smoothing) {
        if (ratings.isEmpty()) {
            return globalMean;
        }
        return (ratings.sum() + smoothing * globalMean) / (ratings.size() + smoothing);
    }

    /**
     * Compute baseline predictions for a user. This uses the rating vector rather than the
     * memorized mean.
     *
     * @param user The user.
     * @param ratings The user's rating vector.
     * @param output The output vector.
     * @param predictSet Whether to predict already-set values in {@code output}.
     */
    @Override
    public void predict(long user, SparseVector ratings,
                        MutableSparseVector output, boolean predictSet) {
        double mean = average(ratings, globalMean, damping);
        //noinspection AssertWithSideEffects
        assert damping != 0 || ratings.isEmpty() || abs(mean - ratings.mean()) < 1.0e-6;
        if (predictSet) {
            output.fill(mean);
        } else {
            for (VectorEntry e : output.fast(VectorEntry.State.UNSET)) {
                output.set(e, mean);
            }
        }
    }

    @Override
    public String toString() {
        String cls = getClass().getSimpleName();
        return String.format("%s(µ=%.3f, γ=%.2f)", cls, globalMean, damping);
    }

}
