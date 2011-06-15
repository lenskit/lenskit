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
package org.grouplens.lenskit.eval.traintest;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.dao.DataAccessObjectManager;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.snapshot.PackedRatingSnapshot;
import org.grouplens.lenskit.data.snapshot.RatingSnapshot;
import org.grouplens.lenskit.data.sql.BasicSQLStatementFactory;
import org.grouplens.lenskit.data.sql.JDBCRatingDAO;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.SharedRatingSnapshot;
import org.grouplens.lenskit.eval.results.AlgorithmTestAccumulator;
import org.grouplens.lenskit.eval.results.ResultAccumulator;
import org.grouplens.lenskit.util.parallel.ExecHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Train-test evaluator that builds on a training set and runs on a test set.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TrainTestPredictEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestPredictEvaluator.class);
    private String databaseUrl;
    private String trainingTable;
    private String testTable;
    private boolean timestamp = true;
    private int threadCount = 0;
    
    public TrainTestPredictEvaluator(String dbUrl, String train, String test) {
        databaseUrl = dbUrl;
        trainingTable = train;
        testTable = test;
    }
    
    public boolean isTimestampEnabled() {
    	return timestamp;
    }
    
    public void setTimestampEnabled(boolean ts) {
    	timestamp = ts;
    }
    
    public void setThreadCount(int nthreads) {
        threadCount = nthreads;
    }
    
    public int getThreadCount() {
        if (threadCount > 0) {
            return threadCount;
        } else {
            int tc = Integer.parseInt(System.getProperty("lenskit.eval.thread.count", "0"));
            if (tc > 0)
                return tc;
            else
                return Runtime.getRuntime().availableProcessors();
        }
    }
    
    protected JDBCRatingDAO.Manager trainingDAOManager() {
        BasicSQLStatementFactory sfac = new BasicSQLStatementFactory();
        sfac.setTableName(trainingTable);
        if (!timestamp)
            sfac.setTimestampColumn(null);
        return new JDBCRatingDAO.Manager(databaseUrl, sfac);
    }
    
    protected JDBCRatingDAO.Manager testDAOManager() {
        BasicSQLStatementFactory testfac = new BasicSQLStatementFactory();
        testfac.setTableName(testTable);
        testfac.setTimestampColumn(null);
        return new JDBCRatingDAO.Manager(databaseUrl, testfac);
    }
    
    public void evaluateAlgorithms(List<AlgorithmInstance> algorithms, 
        final ResultAccumulator results) {
        
        List<Runnable> tasks = makeEvalTasks(algorithms, results);
        
        ExecutorService svc = Executors.newFixedThreadPool(getThreadCount());
        try {
            ExecHelpers.parallelRun(svc, tasks);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error evaluating recommenders",
                                       ExecHelpers.unwrapExecutionException(e));
        } finally {
            svc.shutdown();
        }
    }

    /**
     * Make a list of tasks for the evaluation against this set of algorithms
     * and accumulator.  These can then run in a thread pool if desired.
     * @param algorithms
     * @param results
     * @return
     */
    public List<Runnable> makeEvalTasks(List<AlgorithmInstance> algorithms,
        final ResultAccumulator results) {
        final DataAccessObjectManager<? extends RatingDataAccessObject> daoMgr = trainingDAOManager();
        final DataAccessObjectManager<? extends RatingDataAccessObject> testDaoMgr = testDAOManager();
        final SharedRatingSnapshot snap = loadSnapshot(daoMgr);
        final PreloadCache cache = new PreloadCache();
        
        List<Runnable> tasks = Lists.transform(algorithms, new Function<AlgorithmInstance, Runnable>() {
            public Runnable apply(AlgorithmInstance algo) {
                return new EvalTask(daoMgr, testDaoMgr, results, algo, cache, snap);
            }
        });
        return tasks;
    }

    private SharedRatingSnapshot loadSnapshot(DataAccessObjectManager<? extends RatingDataAccessObject> daoMgr) {
        logger.debug("Preloading rating snapshot data");
        
        RatingDataAccessObject dao = daoMgr.open();
        try {
            return new SharedRatingSnapshot(new PackedRatingSnapshot.Builder(dao).build());
        } finally {
            dao.close();
        }
    }
    
    protected static class PreloadCache {
        private volatile List<Rating> cachedRatings;
        
        public synchronized List<Rating> getRatings(RatingDataAccessObject dao) {
            if (cachedRatings == null) {
                logger.info("Preloading rating data");
                cachedRatings = Cursors.makeList(dao.getRatings());
            }
            return cachedRatings;
        }
    }
    
    protected class EvalTask implements Runnable {
        private AlgorithmInstance algorithm;
        private ResultAccumulator resultAccumulator;
        private DataAccessObjectManager<? extends RatingDataAccessObject> daoManager;
        private DataAccessObjectManager<? extends RatingDataAccessObject> testDaoManager;
        private PreloadCache ratingCache;
        private RatingSnapshot ratingSnapshot;
        
        public EvalTask(DataAccessObjectManager<? extends RatingDataAccessObject> daoMgr,
                DataAccessObjectManager<? extends RatingDataAccessObject> testDaoMgr,
                ResultAccumulator results, AlgorithmInstance algo, PreloadCache cache,
                RatingSnapshot snap) {
            daoManager = daoMgr;
            testDaoManager = testDaoMgr;
            resultAccumulator = results;
            algorithm = algo;
            ratingCache = cache;
            ratingSnapshot = snap;
        }

        @Override
        public void run() {
            AlgorithmTestAccumulator acc = resultAccumulator.makeAlgorithmAccumulator(algorithm);
            RatingDataAccessObject dao = daoManager.open();
            RatingDataAccessObject tdao;

            // Preload ratings if appropriate. Make sure DAO is closed if ratigns
            // are preloaded.
            try {
                if (algorithm.getPreload()) {
                    List<Rating> ratings = ratingCache.getRatings(dao);
                    dao.close();
                    dao = null;
                    tdao = new RatingCollectionDAO(ratings);
                } else {
                    tdao = dao;
                    dao = null;
                }
            } finally {
                if (dao != null)
                    dao.close();
            }

            try {
                logger.info("Building {}", algorithm.getName());
                acc.startBuildTimer();
                Recommender rec = algorithm.buildRecommender(tdao, ratingSnapshot);
                RatingPredictor pred = rec.getRatingPredictor();
                acc.finishBuild();

                logger.info("Testing {}", algorithm.getName());
                acc.startTestTimer();

                RatingDataAccessObject testDao = testDaoManager.open();
                try {
                    Cursor<UserRatingProfile> userProfiles = testDao.getUserRatingProfiles();
                    try {
                        for (UserRatingProfile p: userProfiles) {
                            SparseVector ratings = Ratings.userRatingVector(p.getRatings());
                            SparseVector predictions =
                                pred.predict(p.getUser(), ratings.keySet());
                            acc.evaluatePrediction(p.getUser(), ratings, predictions);
                        }
                    } finally {
                        userProfiles.close();
                    }
                } finally {
                    testDao.close();
                }

                acc.finish();
            } finally {
                tdao.close();
            }
        }

    }
}
