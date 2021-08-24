/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.mf.bpr;


import com.google.common.base.Stopwatch;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.inject.Transient;
import org.lenskit.mf.MFModel;
import org.lenskit.mf.funksvd.FeatureCount;
import org.lenskit.util.keys.HashKeyIndex;
import org.lenskit.util.math.RollingWindowMeanAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
    private final BPRTrainingSampler pairGenerator;
    private final int batchCount;
    private final Random rand;

    private HashKeyIndex itemIndex;
    private HashKeyIndex userIndex;

    @Inject
    public BPRMFModelProvider(@Transient @Nonnull DataAccessObject dao,
                              @Transient @Nonnull BPRTrainingSampler pairGenerator,
                              @FeatureCount int featureCount,
                              @LearningRate double learningRate,
                              @RegularizationTerm double regularization,
                              @IterationCount int batches,
                              @Transient Random rand) {
        this.dao = dao;
        this.pairGenerator = pairGenerator;
        this.featureCount = featureCount;
        this.learningRate = learningRate;
        this.regularization = regularization;
        this.batchCount = batches;
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

        logger.info("Building BPR-MF with {} features for {} users and {} items",
                featureCount, userCount, itemCount);

        Stopwatch timer = Stopwatch.createStarted();

        //REVIEW: because of the nature of training samples (and the point that the BPR paper makes that training
        // by-item or by-user are not optimal) one "iteration" here will be one training update. This leads to _really_
        // big iteration counts, which can actually overflow ints!. one suggestion would be to allow the iteration count
        // controller to accept longs, but I don't know if this will introduce backwards compatibility issues (I imagine
        // this depends on the robustness of our type conversion in the configuration.
        for (int batch = 1; batch <= batchCount; batch++) {
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
            logger.debug("finished iteration {} at {}, log likelihood {}", batch, timer, optAccum.getMean());
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
