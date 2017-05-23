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
package org.lenskit.eval.traintest;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.jcip.annotations.ThreadSafe;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.MergePool;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.LenskitRecommenderEngineBuilder;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.inject.GraphtUtils;
import org.lenskit.inject.NodeProcessors;
import org.lenskit.util.ProgressLogger;
import org.lenskit.util.monitor.TrackedJob;
import org.lenskit.util.parallel.Blockers;
import org.lenskit.util.table.RowBuilder;
import org.lenskit.util.table.writer.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tukaani.xz.UnsupportedOptionsException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Individual job evaluating a single experimental condition.
 */
class ExperimentJob extends RecursiveAction {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentJob.class);
    /**
     * The job type code for experiment jobs.
     * @see TrackedJob#getType()
     */
    public static final String JOB_TYPE = "tt-job";
    public static final String SETUP_JOB_TYPE = "tt-setup";
    public static final String TRAIN_JOB_TYPE = "tt-train";
    public static final String TEST_JOB_TYPE = "tt-test";

    private final TrainTestExperiment experiment;
    private final AlgorithmInstance algorithm;
    private final DataSet dataSet;
    private final LenskitConfiguration sharedConfig;

    @Nullable
    private final ComponentCache cache;
    private final MergePool<Component, Dependency> mergePool;
    private final TrackedJob tracker;
    private final Semaphore limitSemaphore;

    ExperimentJob(TrainTestExperiment exp,
                  @Nonnull AlgorithmInstance algo,
                  @Nonnull DataSet ds,
                  LenskitConfiguration shared,
                  @Nullable ComponentCache cache,
                  @Nullable MergePool<Component, Dependency> pool,
                  TrackedJob tj, @Nullable Semaphore limit) {
        experiment = exp;
        algorithm = algo;
        dataSet = ds;
        sharedConfig = shared;
        this.cache = cache;
        mergePool = pool;
        tracker = tj;
        limitSemaphore = limit;
    }

    @Override
    protected void compute() {
        if (limitSemaphore != null) {
            try {
                Blockers.acquireSemaphore(limitSemaphore);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new EvaluationException("Evaluation interrupted", e);
            }
        }
        try {
            tracker.start();
            doEvaluate();
            tracker.finish();
        } catch (Exception th) {
            if (Thread.interrupted()) {
                logger.info("evaluation of {} on {} interrupted", algorithm, dataSet);
            }
            try {
                logger.error("Error evaluating " + algorithm + " on " + dataSet, th);
                tracker.fail(th);
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            if (th instanceof EvaluationException) {
                throw th;
            } else {
                throw new EvaluationException("Error running evaluation", th);
            }
        } finally {
            if (limitSemaphore != null) {
                limitSemaphore.release();
            }
        }
    }

    /**
     * Inner helper to control the evaluation.
     */
    private void doEvaluate() {
        TrackedJob setup = tracker.makeChild(SETUP_JOB_TYPE);
        TrackedJob train = tracker.makeChild(TRAIN_JOB_TYPE);
        TrackedJob test = tracker.makeChild(TEST_JOB_TYPE);

        setup.start();
        ExperimentOutputLayout layout = experiment.getOutputLayout();
        TableWriter globalOutput = layout.prefixTable(experiment.getGlobalOutput(),
                                                      dataSet, algorithm);
        TableWriter userOutput = layout.prefixTable(experiment.getUserOutput(),
                                                    dataSet, algorithm);
        RowBuilder outputRow = globalOutput.getLayout().newRowBuilder();

        logger.info("fetching training data");
        DataAccessObject trainData = dataSet.getTrainingData().get();

        StaticDataSource rt = dataSet.getRuntimeData();
        DataAccessObject runtimeData = rt != null ? rt.get() : null;
        setup.finish();

        train.start();
        logger.info("Building {} on {}", algorithm, dataSet);
        Stopwatch buildTimer = Stopwatch.createStarted();
        LenskitRecommenderEngine engine;
        try {
             engine = buildRecommenderEngine(trainData);
        } catch (Throwable th) {
            outputRow.add("Succeeded", "N");
            try {
                globalOutput.writeRow(outputRow.buildList());
            } catch (Throwable e) {
                th.addSuppressed(e);
            }
            throw th;
        }
        buildTimer.stop();
        train.finish();
        logger.info("Built {} in {}", algorithm.getName(), buildTimer);
        outputRow.add("BuildTime", buildTimer.elapsed(TimeUnit.MILLISECONDS) * 0.001);
        logger.info("Measuring {} on {}", algorithm.getName(), dataSet.getName());

        List<ConditionEvaluator> accumulators = Lists.newArrayList();

        for (EvalTask task : experiment.getTasks()) {
            ConditionEvaluator ce = task.createConditionEvaluator(algorithm, dataSet, engine);
            if (ce != null) {
                accumulators.add(ce);
            } else {
                logger.warn("Could not instantiate task {} for algorithm {} on data set {}",
                            task, algorithm, dataSet);
            }
        }

        DataAccessObject testData = dataSet.getTestData().get();

        Stopwatch testTimer = Stopwatch.createStarted();

        final NumberFormat pctFormat = NumberFormat.getPercentInstance();
        pctFormat.setMaximumFractionDigits(2);
        pctFormat.setMinimumFractionDigits(2);
        final int nusers = testData.query(CommonTypes.USER).count();
        test.start(nusers);
        logger.info("Testing {} on {} ({} users)", algorithm, dataSet, nusers);
        ProgressLogger progress = ProgressLogger.create(logger)
                                                .setCount(nusers)
                                                .setLabel(String.format("testing users from %s on %s",
                                                                            algorithm.getName(),
                                                                            dataSet.getName()))
                                                .start();

        List<EntityType> entityTypes = dataSet.getEntityTypes();
        logger.info("using entity types {} for test data", entityTypes);
        List<Entity> users = testData.query(CommonTypes.USER).get();
        Stream<Entity> userStream;
        if (inForkJoinPool()) {
            // parallelism is enabled
            userStream = users.parallelStream();
        } else {
            // not parallel, so don't parallelize
            userStream = users.stream();
        }

        UserEvaluator eval = new UserEvaluator(test, userOutput, trainData, runtimeData, engine, accumulators, testData, progress, entityTypes);
        try {
            userStream.forEach(eval);
        } catch (Throwable th) {
            outputRow.add("Succeeded", "N");
            try {
                globalOutput.writeRow(outputRow.buildList());
            } catch (Throwable e) {
                th.addSuppressed(e);
            }
            throw th;
        }

        test.finish();
        progress.finish();
        testTimer.stop();
        logger.info("Tested {} in {}", algorithm.getName(), testTimer);
        outputRow.add("TestTime", testTimer.elapsed(TimeUnit.MILLISECONDS) * 0.001);
        outputRow.add("Succeeded", "Y");
        for (ConditionEvaluator ce : accumulators) {
            outputRow.addAll(ce.finish());
        }

        try {
            globalOutput.writeRow(outputRow.buildList());
            globalOutput.flush();
        } catch (IOException e) {
            throw new EvaluationException("error writing output row", e);
        }
    }

    private LenskitRecommenderEngine buildRecommenderEngine(DataAccessObject train) throws RecommenderBuildException {
        logger.debug("Starting recommender build");

        LenskitRecommenderEngineBuilder builder = new EvalEngineBuilder();
        builder.addConfiguration(sharedConfig);
        builder.addConfiguration(dataSet.getExtraConfiguration());

        for (LenskitConfiguration cfg: algorithm.getConfigurations()) {
            builder.addConfiguration(cfg);
        }

        return builder.build(train);
    }

    private LenskitRecommender buildRecommender(LenskitRecommenderEngine engine,
                                                @Nonnull DataAccessObject train,
                                                @Nullable DataAccessObject runtime) throws RecommenderBuildException {
        if (runtime == null) {
            runtime = train;
        }
        return engine.createRecommender(runtime);
    }

    /**
     * Execute this job immediately.
     */
    public void execute() {
        compute();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new UnsupportedOptionsException("experiment jobs cannot be serialized");
    }

    /**
     * Internal reimplementation of the engine builder to support our manipulations.
     */
    private class EvalEngineBuilder extends LenskitRecommenderEngineBuilder {
        @Override
        protected DAGNode<Component, Dependency> buildRecommenderGraph(DataAccessObject dao) {
            DAGNode<Component, Dependency> graph = super.buildRecommenderGraph(dao);

            if (mergePool != null) {
                logger.debug("deduplicating configuration graph");
                synchronized (mergePool) {
                    graph = mergePool.merge(graph);
                }
            }

            return graph;
        }

        @Override
        protected DAGNode<Component, Dependency> instantiateGraph(DAGNode<Component, Dependency> graph) {
            if (cache == null) {
                logger.debug("Building directly without a cache");
                return super.instantiateGraph(graph);
            } else {
                logger.debug("Instantiating graph with a cache");
                try {
                    Set<DAGNode<Component, Dependency>> nodes = GraphtUtils.getShareableNodes(graph);
                    logger.debug("resolving {} nodes", nodes.size());
                    DAGNode<Component, Dependency> newGraph = NodeProcessors.processNodes(graph, nodes, cache);
                    logger.debug("newGraph went from {} to {} nodes",
                                 newGraph.getReachableNodes().size(),
                                 newGraph.getReachableNodes().size());
                    return newGraph;
                } catch (InjectionException e) {
                    logger.error("Error instantiating recommender nodes with cache", e);
                    throw new RecommenderBuildException("Cached instantiation failed", e);
                }
            }
        }
    }

    @ThreadSafe
    private class UserEvaluator implements Consumer<Entity> {
        private TrackedJob test;
        private TableWriter userOutput;
        private DataAccessObject trainData;
        private DataAccessObject runtimeData;
        private LenskitRecommenderEngine engine;
        private List<ConditionEvaluator> accumulators;
        private DataAccessObject testData;
        private ProgressLogger progress;
        private List<EntityType> entityTypes;

        public UserEvaluator(TrackedJob test, TableWriter userOutput, DataAccessObject trainData, DataAccessObject runtimeData, LenskitRecommenderEngine engine, List<ConditionEvaluator> accumulators, DataAccessObject testData, ProgressLogger progress, List<EntityType> entityTypes) {
            this.test = test;
            this.userOutput = userOutput;
            this.trainData = trainData;
            this.runtimeData = runtimeData;
            this.engine = engine;
            this.accumulators = accumulators;
            this.testData = testData;
            this.progress = progress;
            this.entityTypes = entityTypes;
        }

        @Override
        public void accept(Entity user) {
            try (LenskitRecommender rec = buildRecommender(engine, trainData, runtimeData)) {
                long uid = user.getId();
                RowBuilder userRow = userOutput.getLayout().newRowBuilder();
                userRow.add("User", uid);

                List<Entity> userTrainHistory = new ArrayList<>();
                List<Entity> userTestHistory = new ArrayList<>();

                for (EntityType entityType : entityTypes) {
                    List<Entity> trainHistory = trainData.query(entityType)
                                                         .withAttribute(CommonAttributes.USER_ID, uid)
                                                         .get();

                    userTrainHistory.addAll(trainHistory);

                    List<Entity> testHistory = testData.query(entityType)
                                                       .withAttribute(CommonAttributes.USER_ID, uid)
                                                       .get();

                    userTestHistory.addAll(testHistory);
                }

                TestUser testUser = new TestUser(user, userTrainHistory, userTestHistory);

                Stopwatch userTimer = Stopwatch.createStarted();

                for (ConditionEvaluator eval : accumulators) {
                    Map<String, Object> ures = eval.measureUser(rec, testUser);
                    userRow.addAll(ures);
                }
                userTimer.stop();

                userRow.add("TestTime", userTimer.elapsed(TimeUnit.MILLISECONDS) * 0.001);
                try {
                    userOutput.writeRow(userRow.buildList());
                    userOutput.flush();
                } catch (IOException e) {
                    throw new EvaluationException("error writing user row", e);
                }
                userRow.clear();

                test.finishStep();
                progress.advance();
            }
        }
    }
}
