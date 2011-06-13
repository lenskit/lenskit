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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    public synchronized void evaluateAlgorithms(List<AlgorithmInstance> algorithms, ResultAccumulator results) {
        logger.debug("Preloading rating snapshot data");
        DataAccessObjectManager<? extends RatingDataAccessObject> daoMgr = trainingDAOManager();
        DataAccessObjectManager<? extends RatingDataAccessObject> testDaoMgr = testDAOManager();
        SharedRatingSnapshot snap = loadSnapshot(daoMgr);
        PreloadCache cache = new PreloadCache();
        
        List<Future<?>> tasks = new ArrayList<Future<?>>(algorithms.size());
        ExecutorService svc = Executors.newFixedThreadPool(getThreadCount());
        try {
            for (AlgorithmInstance algo: algorithms) {
                Runnable task = new EvalTask(daoMgr, testDaoMgr, results, algo, cache, snap);
                tasks.add(svc.submit(task));
            }
            for (Future<?> f: tasks) {
                boolean done = false;
                while (!done) {
                    try {
                        f.get();
                        done = true;
                    } catch (InterruptedException e) {
                        /* no-op, try again */
                    } catch (ExecutionException e) {
                        Throwable base = e;
                        if (e.getCause() != null)
                            base = e;
                        base.printStackTrace();
                        throw new RuntimeException("Error testing recommender", base);
                    }
                }
            }
        } finally {
            svc.shutdown();
        }
    }

    private SharedRatingSnapshot loadSnapshot(DataAccessObjectManager<? extends RatingDataAccessObject> daoMgr) {
        RatingDataAccessObject dao;
        dao = daoMgr.open();
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
                logger.debug("Building {}", algorithm.getName());
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
