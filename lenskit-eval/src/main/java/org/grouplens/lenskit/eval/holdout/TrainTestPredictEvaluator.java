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

import java.io.PrintStream;
import java.sql.Connection;
import java.util.List;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.context.AbstractRatingBuildContext;
import org.grouplens.lenskit.data.context.PackedRatingSnapshot;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.context.RatingSnapshot;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.sql.BasicSQLStatementFactory;
import org.grouplens.lenskit.data.sql.JDBCRatingDAO;
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
    private Connection connection;
    private String trainingTable;
    private String testTable;
    private PrintStream progressStream;

    public TrainTestPredictEvaluator(Connection dbc, String train, String test) {
        connection = dbc;
        trainingTable = train;
        testTable = test;
    }
    
    /**
     * Set print stream for outputting progress within evaluations.
     * @param s
     */
    public void setProgressStream(PrintStream s) {
        progressStream = s;
    }
    
    public void evaluateAlgorithms(List<AlgorithmInstance> algorithms, ResultAccumulator results) {
        BasicSQLStatementFactory sfac = new BasicSQLStatementFactory();
        sfac.setTableName(trainingTable);
        JDBCRatingDAO dao = new JDBCRatingDAO(null, sfac);
        dao.openSession(connection);
        
        RatingDataAccessObject preloaded = null;
        logger.debug("Preloading rating snapshot data");
        PackedRatingSnapshot snap = PackedRatingSnapshot.make(dao);
        
        BasicSQLStatementFactory testfac = new BasicSQLStatementFactory();
        testfac.setTableName(testTable);
        testfac.setTimestampColumn(null);
        JDBCRatingDAO testDao = new JDBCRatingDAO(null, testfac);
        testDao.openSession(connection);
        try {
            int nusers = testDao.getUserCount();
            logger.debug("Evaluating algorithms with {} users", nusers);
            
            for (AlgorithmInstance algo: algorithms) {
                AlgorithmTestAccumulator acc = results.makeAlgorithmAccumulator(algo);
                RecommenderComponentBuilder<RecommenderEngine> builder = algo.getBuilder();
                RatingDataAccessObject tdao;
                if (algo.getPreload()) {
                	if (preloaded == null) {
                		logger.info("Preloading rating data for {}", algo.getName());
                		List<Rating> ratings = Cursors.makeList(dao.getRatings());
                		preloaded = new RatingCollectionDAO(ratings);
                		preloaded.openSession();
                	}
                	tdao = preloaded;
                } else {
                	tdao = dao;
                }
                logger.debug("Building {}", algo.getName());
                acc.startBuildTimer();
                RatingBuildContext rbc = new BuildContext(tdao, snap);
                RecommenderEngine rec;
                try {
                	rec = builder.build(rbc);
                } finally {
                	rbc.close();
                }
                RatingPredictor pred = rec.getRatingPredictor();
                acc.finishBuild();

                logger.info("Testing {}", algo.getName());
                acc.startTestTimer();

                Cursor<UserRatingProfile> userProfiles = testDao.getUserRatingProfiles();
                try {
                    int n = 0;
                    for (UserRatingProfile p: userProfiles) {
                        if (progressStream != null) {
                            progressStream.format("users: %d / %d\r", n, nusers);
                        }
                        
                        SparseVector ratings = Ratings.userRatingVector(p.getRatings());
                        SparseVector predictions =
                            pred.predict(p.getUser(), ratings.keySet());
                        acc.evaluatePrediction(p.getUser(), ratings, predictions);
                        n++;
                    }
                    if (progressStream != null)
                        progressStream.format("tested users: %d / %d\n", n, nusers);
                } finally {
                    userProfiles.close();
                }

                acc.finish();
                tdao = null;
            }
        } finally {
        	if (preloaded != null)
        		preloaded.closeSession();
            dao.closeSession();
            testDao.closeSession();
        }
    }
    
    private static class BuildContext extends AbstractRatingBuildContext {
    	private RatingSnapshot snapshot;
		public BuildContext(RatingDataAccessObject dao, PackedRatingSnapshot snap) {
			super(dao);
			snapshot = snap;
		}
		@Override
		public RatingSnapshot ratingSnapshot() {
			return snapshot;
		}
		@Override
		public RatingSnapshot trainingSnapshot() {
			throw new UnsupportedOperationException();
		}
		@Override
		public RatingSnapshot tuningSnapshot() {
			throw new UnsupportedOperationException();
		}
    	
    }
}
