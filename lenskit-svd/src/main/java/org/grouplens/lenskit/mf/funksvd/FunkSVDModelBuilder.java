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
package org.grouplens.lenskit.mf.funksvd;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * SVD recommender builder using gradient descent (Funk SVD).
 *
 * This recommender builder constructs an SVD-based recommender using gradient
 * descent, as pioneered by Simon Funk.  It also incorporates the regularizations
 * Funk did. These are documented in
 * <a href="http://sifter.org/~simon/journal/20061211.html">Netflix Update: Try
 * This at Home</a>. This implementation is based in part on
 * <a href="http://www.timelydevelopment.com/demos/NetflixPrize.aspx">Timely
 * Development's sample code</a>.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@NotThreadSafe
public class FunkSVDModelBuilder implements Provider<FunkSVDModel> {
    private static Logger logger = LoggerFactory.getLogger(FunkSVDModelBuilder.class);

    private final int featureCount;
    private final BaselinePredictor baseline;
    private final PreferenceSnapshot snapshot;
    private final double initialValue;

    private FunkSVDUpdateRule rule;

    @Inject
    public FunkSVDModelBuilder(@Transient @Nonnull PreferenceSnapshot snapshot,
                               @Transient @Nonnull FunkSVDUpdateRule rule,
                               @Nonnull BaselinePredictor baseline,
                               @FeatureCount int featureCount,
                               @InitialFeatureValue double initVal) {
        this.featureCount = featureCount;
        this.initialValue = initVal;
        this.baseline = baseline;
        this.snapshot = snapshot;
        this.rule = rule;
    }


    @Override
    public FunkSVDModel get() {
        double[][] userFeatures = new double[featureCount][snapshot.getUserIds().size()];
        double[][] itemFeatures = new double[featureCount][snapshot.getItemIds().size()];

        logger.debug("Setting up to build SVD recommender with {} features", featureCount);
        logger.debug("Learning rate is {}", rule.getLearningRate());
        logger.debug("Regularization term is {}", rule.getTrainingRegularization());

        FastCollection<IndexedPreference> ratings = snapshot.getRatings();
        logger.debug("Building SVD with {} features for {} ratings", featureCount, ratings.size());

        TrainingEstimator estimates = rule.makeEstimator(snapshot);

        List<FeatureInfo> featureInfo = new ArrayList<FeatureInfo>(featureCount);

        for (int f = 0; f < featureCount; f++) {
            logger.trace("Training feature {}", f);
            // We assume that all subsequent features have initialValue
            // We can therefore pre-compute the "trailing" prediction value, as it
            // will be the same for all ratings for this feature.
            final double trail = (featureCount - f - 1) * initialValue * initialValue;

            // Fetch and initialize the arrays for this feature
            DoubleArrays.fill(userFeatures[f], initialValue);
            DoubleArrays.fill(itemFeatures[f], initialValue);

            FeatureInfo info = rule.trainFeature(estimates, ratings,
                                                 userFeatures[f], itemFeatures[f],
                                                 trail);
            featureInfo.add(info);

            // Update each rating's cached value to accommodate the feature values.
            estimates.update(userFeatures[f], itemFeatures[f]);
        }

        return new FunkSVDModel(featureCount, itemFeatures, userFeatures,
                                rule.getClampingFunction(),
                                snapshot.itemIndex(), snapshot.userIndex(),
                                baseline, featureInfo);
    }
}
