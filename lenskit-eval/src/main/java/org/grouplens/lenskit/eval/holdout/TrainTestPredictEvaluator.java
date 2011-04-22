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
package org.grouplens.lenskit.eval.holdout;

import java.util.List;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.context.PackedRatingBuildContext;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.results.AlgorithmTestAccumulator;
import org.grouplens.lenskit.eval.results.ResultAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Train-test evaluator that builds on a training set and runs on a test set.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TrainTestPredictEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestPredictEvaluator.class);
    private RatingDataAccessObject trainingDao;
    private RatingDataAccessObject testDao;

    public TrainTestPredictEvaluator(RatingDataAccessObject train,
            RatingDataAccessObject test) {
        trainingDao = train;
        testDao = test;
    }
    
    public void evaluateAlgorithms(List<AlgorithmInstance> algorithms, ResultAccumulator results) {
        for (AlgorithmInstance algo: algorithms) {
            AlgorithmTestAccumulator acc = results.makeAlgorithmAccumulator(algo);
            RecommenderComponentBuilder<RecommenderEngine> builder = algo.getBuilder();
            logger.debug("Building {}", algo.getName());
            acc.startBuildTimer();
            RatingBuildContext rbc = PackedRatingBuildContext.make(trainingDao);
            RecommenderEngine rec = builder.build(rbc);
            RatingPredictor pred = rec.getRatingPredictor();
            acc.finishBuild();
            
            logger.debug("Testing {}", algo.getName());
            acc.startTestTimer();
            
            Cursor<UserRatingProfile> profiles = testDao.getUserRatingProfiles();
            try {
                for (UserRatingProfile p: profiles) {
                    SparseVector ratings = Ratings.userRatingVector(p.getRatings());
                    SparseVector predictions =
                        pred.predict(p.getUser(), ratings.keySet());
                    acc.evaluatePrediction(p.getUser(), ratings, predictions);
                }
            } finally {
                profiles.close();
            }
            
            acc.finish();
        }
    }
}
