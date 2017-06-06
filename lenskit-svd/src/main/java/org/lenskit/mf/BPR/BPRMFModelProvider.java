/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.mf.BPR;


import org.apache.commons.math3.linear.*;
import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.RatingMatrix;
import org.lenskit.inject.Transient;
import org.lenskit.mf.funksvd.FeatureCount;
import org.lenskit.mf.svd.MFModel;
import org.lenskit.util.keys.HashKeyIndex;
import org.lenskit.util.keys.KeyIndex;
import org.lenskit.util.math.RollingWindowMeanAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Random;

import static java.lang.Math.exp;

/**
 * This class trains a MF Model based on the BPR-Optimization Criteria
 */
public class BPRMFModelProvider implements Provider<MFModel> {

    private static Logger logger = LoggerFactory.getLogger(BPRMFModelProvider.class);

    private final int featureCount;
    private final double learningRate;
    private final double regularization;
    private final DataAccessObject dao;
    private final StoppingCondition stoppingCondition;
    private final TrainingPairGenerator pairGenerator;
    private final Random rand;

    private HashKeyIndex itemIndex;
    private HashKeyIndex userIndex;

    @Inject
    public BPRMFModelProvider(@Transient @Nonnull DataAccessObject dao,
                              @Transient @Nonnull TrainingPairGenerator pairGenerator,
                              @FeatureCount int featureCount,
                              @LearningRate double learningRate,
                              @RegularizationTerm double regularizaiton,
                              @Transient StoppingCondition stoppingCondition,
                              @Transient Random rand) {
        this.dao = dao;
        this.pairGenerator = pairGenerator;
        this.featureCount = featureCount;
        this.learningRate = learningRate;
        this.regularization = regularizaiton;
        this.stoppingCondition = stoppingCondition;
        this.rand = rand;

        itemIndex = new HashKeyIndex();
        userIndex = new HashKeyIndex();
    }

    @Override
    public MFModel get() {
        // This will accumulate BPR-Opt (minus the regularization) and will be negated to make an error.
        // -30 is arbitrary, but would indicate a _really_ consistently bad prediction (~ p=1*10^-13),
        // and is therefore a reasonable "max_error".
        RollingWindowMeanAccumulator optAccum = new RollingWindowMeanAccumulator(10000, -30);

        // set up user index and matrix
        int userCount = dao.getEntityIds(CommonTypes.USER).size();
        for(long uid : dao.getEntityIds(CommonTypes.USER)) {
            userIndex.internId(uid);
        }
        RealMatrix userFeatures = MatrixUtils.createRealMatrix(userCount, featureCount);
        initFeatures(userFeatures);

        // set up item index and matrix
        int itemCount = dao.getEntityIds(CommonTypes.ITEM).size();
        for(long iid : dao.getEntityIds(CommonTypes.ITEM)) {
            itemIndex.internId(iid);
        }
        RealMatrix itemFeatures = MatrixUtils.createRealMatrix(itemCount, featureCount);
        initFeatures(itemFeatures);

        logger.debug("Learning rate is {}", learningRate);
        logger.debug("Regularization term is {}", regularization);

        logger.info("Building MPR-MF with {} features for {} users and {} items",
                featureCount, userCount, itemCount);

        TrainingLoopController controller = stoppingCondition.newLoop();

        //REVIEW: because of the nature of training samples (and the point that the BPR paper makes that training
        // by-item or by-user are not optimal) one "iteration" here will be one training update. This leads to _really_
        // big iteration counts, which can actually overflow ints!. one suggestion would be to allow the iteration count
        // controller to accept longs, but I don't know if this will introduce backwards compatibility issues (I imagine
        // this depends on the robustness of our type conversion in the configuration.
        while(controller.keepTraining(-optAccum.getMean())) {
            for (TrainingItemPair pair : pairGenerator.nextBatch()) {
                // Note: bad code style variable names are generally to match BPR paper and enable easier implementation
                long iid = pair.g;
                int i = itemIndex.internId(iid);
                long jid = pair.l;
                int j = itemIndex.internId(jid);
                long uid = pair.u;
                int u = userIndex.internId(uid);

                RealVector w_u = userFeatures.getRowVector(u);
                RealVector h_i = itemFeatures.getRowVector(i);
                RealVector h_j = itemFeatures.getRowVector(j);

                double xui = w_u.dotProduct(h_i);
                double xuj = w_u.dotProduct(h_j);
                double xuij = xui - xuj;

                double bprTerm = 1 / (1 + exp(xuij));

                // w_u update
                RealVector h_i_j = h_i.subtract(h_j);
                RealVector w_u_update = w_u.mapMultiply(-regularization);
                w_u_update.combineToSelf(1, bprTerm, h_i_j);

                // h_i update
                RealVector h_i_update = h_i.mapMultiply(-regularization);
                h_i_update.combineToSelf(1, bprTerm, w_u);

                // h_j update
                RealVector h_j_update = h_j.mapMultiply(-regularization);
                h_j_update.combineToSelf(1, -bprTerm, w_u);

                // perform updates
                w_u.combineToSelf(1, learningRate, w_u_update);
                h_i.combineToSelf(1, learningRate, h_i_update);
                h_j.combineToSelf(1, learningRate, h_j_update);

                // update the optimization function accumulator (note we are not including the regularization term)
                optAccum.add(Math.log(1 / (1 + Math.exp(-xuij))));
            }
        }

        return new MFModel(userFeatures, itemFeatures, userIndex, itemIndex);
    }

    /**
     * Initialize the feature vectors.
     **/
    private void initFeatures(RealMatrix features) {
        features.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(int row, int column, double value) {
                return rand.nextDouble();
            }
        });
    }
}
