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
package org.grouplens.lenskit.basic;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer.makeRatingVector;

/**
 * Basic {@link org.grouplens.lenskit.RatingPredictor} backed by an
 * {@link org.grouplens.lenskit.ItemScorer}.  The scores are clamped to the preference domain
 * but otherwise unmodified.
 *
 * <p>If a baseline predictor is provided, then it is used to supply predictions that the item
 * scorer could not.
 *
 * <p>This class has a provider {@link SimpleRatingPredictor.Provider} that is the default provider
 * for {@link RatingPredictor}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public final class SimpleRatingPredictor extends AbstractRatingPredictor {
    private final ItemScorer scorer;
    @Nullable
    private final BaselinePredictor baselinePredictor;
    @Nullable
    private final PreferenceDomain preferenceDomain;

    @Inject
    public SimpleRatingPredictor(DataAccessObject dao, ItemScorer scorer,
                                 @Nullable BaselinePredictor baseline,
                                 @Nullable PreferenceDomain domain) {
        // TODO Make abstract rating predictors & item scorers not need the DAO
        super(dao);
        this.scorer = scorer;
        baselinePredictor = baseline;
        preferenceDomain = domain;
    }

    /**
     * Get the backing scorer.
     *
     * @return The item scorer.
     */
    public ItemScorer getScorer() {
        return scorer;
    }

    /**
     * Get the baseline predictor.
     *
     * @return The baseline predictor, or {@code null} if no baseline is configured.
     */
    @Nullable
    public BaselinePredictor getBaselinePredictor() {
        return baselinePredictor;
    }

    @Override
    public void predict(long user, @Nonnull MutableSparseVector scores) {
        scorer.score(user, scores);
        if (baselinePredictor != null) {
            baselinePredictor.predict(user, scores, false);
        }
        if (preferenceDomain != null) {
            preferenceDomain.clampVector(scores);
        }
    }

    @Override
    public void predict(@Nonnull UserHistory<? extends Event> profile, @Nonnull MutableSparseVector predictions) {
        scorer.score(profile, predictions);
        if (baselinePredictor != null) {
            baselinePredictor.predict(profile.getUserId(), makeRatingVector(profile), predictions, false);
        }
        if (preferenceDomain != null) {
            preferenceDomain.clampVector(predictions);
        }
    }

    /**
     * An intelligent provider for simple rating predictors. It provides a simple rating predictor
     * if there is an {@link ItemScorer} available, and returns {@code null} otherwise.  This is
     * the default provider for {@link RatingPredictor}
     */
    public static class Provider implements javax.inject.Provider<RatingPredictor> {
        private final DataAccessObject dao;
        private final ItemScorer scorer;
        private final BaselinePredictor baseline;
        private final PreferenceDomain domain;

        @Inject
        public Provider(DataAccessObject dao,
                        @Nullable ItemScorer s,
                        @Nullable BaselinePredictor bp,
                        @Nullable PreferenceDomain dom) {
            this.dao = dao;
            scorer = s;
            baseline = bp;
            domain = dom;
        }

        @Override
        public RatingPredictor get() {
            if (scorer == null) {
                return null;
            } else {
                return new SimpleRatingPredictor(dao, scorer, baseline, domain);
            }
        }
    }
}
