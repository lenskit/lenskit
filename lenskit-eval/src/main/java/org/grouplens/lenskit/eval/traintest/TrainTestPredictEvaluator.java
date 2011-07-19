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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.snapshot.PackedRatingSnapshot;
import org.grouplens.lenskit.data.snapshot.RatingSnapshot;
import org.grouplens.lenskit.data.sql.BasicSQLStatementFactory;
import org.grouplens.lenskit.data.sql.JDBCRatingDAO;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.data.vector.UserRatingVector;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.SharedRatingSnapshot;
import org.grouplens.lenskit.eval.results.AlgorithmTestAccumulator;
import org.grouplens.lenskit.eval.results.ResultAccumulator;
import org.grouplens.lenskit.util.LazyValue;
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
    private String name;
    
    public TrainTestPredictEvaluator(String dbUrl, String train, String test) {
        databaseUrl = dbUrl;
        name = dbUrl;
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
    
    /**
     * Get the identifying name of this evaluator. The default name is the database
     * URL.
     * @return The name provided by {@link #setName(String)} or the database URL.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Provide a meaningful name for this evaluator.
     * @param n The evaluator's name.
     */
    public void setName(String n) {
        name = n;
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
    
    protected JDBCRatingDAO.Factory trainingDAOManager() {
        BasicSQLStatementFactory sfac = new BasicSQLStatementFactory();
        sfac.setTableName(trainingTable);
        if (!timestamp)
            sfac.setTimestampColumn(null);
        return new JDBCRatingDAO.Factory(databaseUrl, sfac);
    }
    
    protected JDBCRatingDAO.Factory testDAOManager() {
        BasicSQLStatementFactory testfac = new BasicSQLStatementFactory();
        testfac.setTableName(testTable);
        testfac.setTimestampColumn(null);
        return new JDBCRatingDAO.Factory(databaseUrl, testfac);
    }
    
    /**
     * Evaluate a set of algorithms.
     * @param recipe The evaluation recipe.
     * @deprecated Use {@link #runEvaluation(EvaluationRecipe)} instead.  This
     * method will be removed in a future version of LensKit.
     */
    @Deprecated
    public void evaluateAlgorithms(EvaluationRecipe recipe) {
        runEvaluation(recipe);
    }
    
    /**
     * Run a train-test evaluation with an evaluation recipe.
     * @param recipe The evaluation recipe to execute.
     */
    public void runEvaluation(EvaluationRecipe recipe) {
        
        List<Runnable> tasks = makeEvalTasks(recipe);
        
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
     * Make a list of tasks for running this evaluation recipe.
     * @param recipe An evaluation recipe to run.
     * @return The tasks to run to perform a train-test evaluation on the algorithms.
     */
    public List<Runnable> makeEvalTasks(EvaluationRecipe recipe) {
        
        final DAOFactory daoMgr = trainingDAOManager();
        final DAOFactory testDaoMgr = testDAOManager();
        
        final ResultAccumulator accum = recipe.makeAccumulator(getName());
        
        final LazyValue<SharedRatingSnapshot> snap =
            new LazyValue<SharedRatingSnapshot>(new Callable<SharedRatingSnapshot>() {
                @Override
                public SharedRatingSnapshot call() {
                    logger.info("Loading snapshot for {}", name);
                    return loadSnapshot(daoMgr);
                }
            });
        final LazyValue<List<Event>> preload =
            new LazyValue<List<Event>>(new Callable<List<Event>>() {
                @Override
                public List<Event> call() {
                    logger.info("Preloading ratings for {}", name);
                    DataAccessObject dao = daoMgr.create();
                    try {
                        return Cursors.makeList(dao.getEvents());
                    } finally {
                        dao.close();
                    }
                }
            });
        
        List<Runnable> tasks = Lists.transform(recipe.getAlgorithms(),
                                               new Function<AlgorithmInstance, Runnable>() {
            @Override
            public Runnable apply(AlgorithmInstance algo) {
                return new EvalTask(daoMgr, testDaoMgr, accum, algo, preload, snap);
            }
        });
        return tasks;
    }

    private SharedRatingSnapshot loadSnapshot(DAOFactory daoMgr) {
        DataAccessObject dao = daoMgr.create();
        try {
            return new SharedRatingSnapshot(new PackedRatingSnapshot.Builder(dao).build());
        } finally {
            dao.close();
        }
    }
    
    protected class EvalTask implements Runnable {
        private AlgorithmInstance algorithm;
        private ResultAccumulator resultAccumulator;
        private DAOFactory daoManager;
        private DAOFactory testDaoManager;
        private LazyValue<List<Event>> ratingCache;
        private LazyValue<? extends RatingSnapshot> ratingSnapshot;
        
        public EvalTask(DAOFactory daoMgr,
                DAOFactory testDaoMgr,
                ResultAccumulator results, AlgorithmInstance algo,
                LazyValue<List<Event>> cache,
                LazyValue<? extends RatingSnapshot> snap) {
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
            DataAccessObject dao;

            // Preload ratings if appropriate.
            if (algorithm.getPreload()) {
                dao = new EventCollectionDAO(ratingCache.get());
            } else {
                dao = daoManager.create();
            }

            try {
                logger.info("Building {}", algorithm.getName());
                acc.startBuildTimer();
                Recommender rec = algorithm.buildRecommender(dao, ratingSnapshot.get());
                RatingPredictor pred = rec.getRatingPredictor();
                acc.finishBuild();

                logger.info("Testing {}", algorithm.getName());
                acc.startTestTimer();

                DataAccessObject testDao = testDaoManager.create();
                try {
                    Cursor<UserHistory<Rating>> userProfiles = testDao.getUserHistories(Rating.class);
                    try {
                        for (UserHistory<Rating> p: userProfiles) {
                            SparseVector ratings = UserRatingVector.fromRatings(p);
                            SparseVector predictions =
                                pred.predict(p.getUserId(), ratings.keySet());
                            acc.evaluatePrediction(p.getUserId(), ratings, predictions);
                        }
                    } finally {
                        userProfiles.close();
                    }
                } finally {
                    testDao.close();
                }

                acc.finish();
            } finally {
                dao.close();
            }
        }

    }
}
