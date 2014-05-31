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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import com.google.common.io.Closer;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.MergePool;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.RecommenderConfigurationException;
import org.grouplens.lenskit.eval.AbstractTask;
import org.grouplens.lenskit.eval.ExecutionInfo;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstanceBuilder;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.Metric;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.util.parallel.TaskGraphExecutor;
import org.grouplens.lenskit.util.table.Table;
import org.grouplens.lenskit.util.table.TableBuilder;
import org.grouplens.lenskit.util.table.TableLayout;
import org.grouplens.lenskit.util.table.writer.CSVWriter;
import org.grouplens.lenskit.util.table.writer.MultiplexedTableWriter;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * The command that run the algorithmInfo instance and output the prediction result file and the evaluation result file
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TrainTestEvalTask extends AbstractTask<Table> {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestEvalTask.class);

    private List<TTDataSet> dataSets;
    private List<AlgorithmInstance> algorithms;
    private List<ExternalAlgorithm> externalAlgorithms;
    private List<MetricFactory> metrics;
    private List<Pair<Symbol,String>> predictChannels;
    private boolean isolate;
    private boolean separateAlgorithms;
    private File outputFile;
    private File userOutputFile;
    private File predictOutputFile;
    private File recommendOutputFile;
    private File cacheDir;
    private File taskGraphFile;
    private File taskStatusFile;
    private boolean cacheAll = false;

    private ExperimentSuite experiments;
    private MeasurementSuite measurements;
    private ExperimentOutputLayout layout;
    private ExperimentOutputs outputs;

    public TrainTestEvalTask() {
        this("train-test");
    }

    public TrainTestEvalTask(String name) {
        super(name);
        dataSets = Lists.newArrayList();
        algorithms = Lists.newArrayList();
        externalAlgorithms = Lists.newArrayList();
        metrics = Lists.newArrayList();
        predictChannels = Lists.newArrayList();
        outputFile = new File("train-test-results.csv");
        isolate = false;
    }

    public TrainTestEvalTask addDataset(TTDataSet source) {
        dataSets.add(source);
        return this;
    }

    public TrainTestEvalTask addAlgorithm(AlgorithmInstance algorithm) {
        algorithms.add(algorithm);
        return this;
    }

    public TrainTestEvalTask addAlgorithm(Map<String,Object> attrs, String file) throws IOException, RecommenderConfigurationException {
        algorithms.add(new AlgorithmInstanceBuilder().setProject(getProject())
                                                     .configureFromFile(attrs, new File(file))
                                                     .build());
        return this;
    }

    public TrainTestEvalTask addExternalAlgorithm(ExternalAlgorithm algorithm) {
        externalAlgorithms.add(algorithm);
        return this;
    }

    public TrainTestEvalTask addMetric(Metric metric) {
        metrics.add(MetricFactory.forMetric(metric));
        return this;
    }

    public TrainTestEvalTask addMetric(Class<? extends Metric> metricClass) throws IllegalAccessException, InstantiationException {
        return addMetric(metricClass.newInstance());
    }

    /**
     * Add a metric that may write multiple columns per algorithmInfo.
     * @param file The output file.
     * @param columns The column headers.
     * @param metric The metric function. It should return a list of table rows, each of which has
     *               entries corresponding to each column.
     * @return The command (for chaining).
     */
    public TrainTestEvalTask addMultiMetric(File file, List<String> columns, Function<Recommender,List<List<Object>>> metric) {
        metrics.add(new FunctionMultiModelMetric.Factory(file, columns, metric));
        return this;
    }

    /**
     * Add a metric that takes some metric of an algorithmInfo.
     * @param columns The column headers.
     * @param metric The metric function. It should return a list of table rows, each of which has
     *               entries corresponding to each column.
     * @return The command (for chaining).
     */
    public TrainTestEvalTask addMetric(List<String> columns, Function<Recommender,List<Object>> metric) {
        addMetric(new FunctionModelMetric(columns, metric));
        return this;
    }

    /**
     * Add a channel to be recorded with predict output.
     *
     * @param channelSym The channel to output.
     * @return The command (for chaining)
     * @see #addWritePredictionChannel
     */
    public TrainTestEvalTask addWritePredictionChannel(@Nonnull Symbol channelSym) {
        return addWritePredictionChannel(channelSym, null);
    }

    /**
     * Add a channel to be recorded with predict output.
     *
     * @param channelSym The channel to record, if present in the prediction output vector.
     * @param label   The column label. If {@code null}, the channel symbol's name is used.
     * @return The command (for chaining).
     * @see #setPredictOutput(File)
     */
    public TrainTestEvalTask addWritePredictionChannel(@Nonnull Symbol channelSym,
                                                       @Nullable String label) {
        Preconditions.checkNotNull(channelSym, "channel is null");
        if (label == null) {
            label = channelSym.getName();
        }
        Pair<Symbol, String> entry = Pair.of(channelSym, label);
        predictChannels.add(entry);
        return this;
    }

    public TrainTestEvalTask setOutput(File file) {
        outputFile = file;
        return this;
    }

    public TrainTestEvalTask setOutput(String fn) {
        return setOutput(new File(fn));
    }

    public TrainTestEvalTask setUserOutput(File file) {
        userOutputFile = file;
        return this;
    }

    public TrainTestEvalTask setUserOutput(String fn) {
        return setUserOutput(new File(fn));
    }

    public TrainTestEvalTask setPredictOutput(File file) {
        predictOutputFile = file;
        return this;
    }

    public TrainTestEvalTask setPredictOutput(String fn) {
        return setPredictOutput(new File(fn));
    }

    public TrainTestEvalTask setRecommendOutput(File file) {
        recommendOutputFile = file;
        return this;
    }

    public TrainTestEvalTask setRecommendOutput(String fn) {
        return setRecommendOutput(new File(fn));
    }

    /**
     * Set the component cache directory.  This directory is used for caching components that
     * might take a lot of memory but are shared between runs.  Only meaningful if algorithms
     * are not separated.
     *
     * @param file The cache directory.
     * @return The task (for chaining)
     * @see #setSeparateAlgorithms(boolean)
     */
    public TrainTestEvalTask setComponentCacheDirectory(File file) {
        cacheDir = file;
        return this;
    }

    /**
     * Set the component cache directory by name.
     *
     * @see #setComponentCacheDirectory(java.io.File)
     */
    public TrainTestEvalTask setComponentCacheDirectory(String fn) {
        return setComponentCacheDirectory(new File(fn));
    }

    public File getComponentCacheDirectory() {
        return cacheDir;
    }

    /**
     * Control whether all components are cached.  Caching all components will use more disk space,
     * but may allow future evaluations to re-use component instances from prior runs.
     *
     * @param flag {@code true} to cache all components.
     * @return The task (for chaining).
     */
    public TrainTestEvalTask setCacheAllComponents(boolean flag) {
        cacheAll = flag;
        return this;
    }

    public boolean getCacheAllComponents() {
        return cacheAll;
    }

    /**
     * Control whether the train-test evaluator will isolate data sets.  If set to {@code true},
     * then each data set will be run in turn, with no inter-data-set parallelism.  This can
     * reduce memory usage for some large runs, keeping the data from only a single data set in
     * memory at a time.  Otherwise (the default), individual algorithmInfo/data-set runs may be freely
     * intermingled.
     *
     * @param iso Whether to isolate data sets.
     * @return The task (for chaining).
     * @deprecated Isolate data sets instead.
     */
    @Deprecated
    public TrainTestEvalTask setIsolate(boolean iso) {
        logger.warn("Eval task isolation is deprecated. Isolate data sets instead.");
        isolate = iso;
        return this;
    }

    /**
     * Control whether the evaluator separates or combines algorithms.  If set to {@code true}, each
     * algorithm instance is built without reusing components from other algorithm instances.
     * By default, LensKit will try to reuse common components between algorithm configurations
     * (one downside of this is that it makes build timings meaningless).
     *
     * @param sep If {@code true}, separate algorithms.
     * @return The task (for chaining).
     */
    public TrainTestEvalTask setSeparateAlgorithms(boolean sep) {
        separateAlgorithms = sep;
        return this;
    }

    List<TTDataSet> dataSources() {
        return dataSets;
    }

    List<AlgorithmInstance> getAlgorithms() {
        return algorithms;
    }

    List<ExternalAlgorithm> getExternalAlgorithms() {
        return externalAlgorithms;
    }

    List<MetricFactory> getMetricFactories() {
        return metrics;
    }

    List<Pair<Symbol,String>> getPredictionChannels() {
        return predictChannels;
    }

    File getOutput() {
        return outputFile;
    }

    File getPredictOutput() {
        return predictOutputFile;
    }

    File getRecommendOutput() {
        return recommendOutputFile;
    }

    /**
     * Set an output file for a description of the task graph.
     * @param f The output file.
     * @return The task (for chaining).
     */
    public TrainTestEvalTask setTaskGraphFile(File f) {
        taskGraphFile = f;
        return this;
    }

    /**
     * Set an output file for a description of the task graph.
     * @param f The output file name
     * @return The task (for chaining).
     */
    public TrainTestEvalTask setTaskGraphFile(String f) {
        return setTaskGraphFile(new File(f));
    }

    public File getTaskGraphFile() {
        return taskGraphFile;
    }

    /**
     * Set an output file for task status updates.
     * @param f The output file.
     * @return The task (for chaining).
     */
    public TrainTestEvalTask setTaskStatusFile(File f) {
        taskStatusFile = f;
        return this;
    }

    /**
     * Set an output file for task status updates.
     * @param f The output file name
     * @return The task (for chaining).
     */
    public TrainTestEvalTask setTaskStatusFile(String f) {
        return setTaskStatusFile(new File(f));
    }

    public File getTaskStatusFile() {
        return taskStatusFile;
    }

    /**
     * Run the evaluation on the train test data source files
     *
     * @return The summary output table.
     * @throws org.grouplens.lenskit.eval.TaskExecutionException
     *          Failure of the evaluation
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Table perform() throws TaskExecutionException, InterruptedException {
        try {
            experiments = createExperimentSuite();
            measurements = createMeasurementSuite();
            layout = ExperimentOutputLayout.create(experiments, measurements);
            TableBuilder resultsBuilder = new TableBuilder(layout.getResultsLayout());

            logger.info("Starting evaluation of {} algorithms ({} from LensKit) on {} data sets",
                        Iterables.size(experiments.getAllAlgorithms()),
                        experiments.getAlgorithms().size(),
                        experiments.getDataSets().size());
            Closer closer = Closer.create();

            try {
                outputs = openExperimentOutputs(layout, measurements, resultsBuilder, closer);
                DAGNode<JobGraph.Node,JobGraph.Edge> jobGraph;
                try {
                    jobGraph = makeJobGraph(experiments);
                } catch (RecommenderConfigurationException ex) {
                    throw new TaskExecutionException("Recommender configuration error", ex);
                }
                if (taskGraphFile != null) {
                    logger.info("writing task graph to {}", taskGraphFile);
                    JobGraph.writeGraphDescription(jobGraph, taskGraphFile);
                }
                registerTaskListener(jobGraph);

                // tell all metrics to get started
                runEvaluations(jobGraph);
            } catch (Throwable th) {
                throw closer.rethrow(th, TaskExecutionException.class, InterruptedException.class);
            } finally {
                closer.close();
            }

            logger.info("evaluation {} completed", getName());

            return resultsBuilder.build();
        } catch (IOException e) {
            throw new TaskExecutionException("I/O error", e);
        } finally {
            experiments = null;
            measurements = null;
            outputs = null;
            layout = null;
        }
    }

    private void registerTaskListener(DAGNode<JobGraph.Node, JobGraph.Edge> jobGraph) {
        if (taskStatusFile != null) {
            ImmutableSet.Builder<TrainTestJob> jobs = ImmutableSet.builder();
            for (DAGNode<JobGraph.Node, JobGraph.Edge> node: jobGraph.getReachableNodes()) {
                TrainTestJob job = node.getLabel().getJob();
                if (job != null) {
                    jobs.add(job);
                }
            }
            JobStatusWriter monitor = new JobStatusWriter(this, jobs.build(), taskStatusFile);
            getProject().getEventBus().register(monitor);
        }
    }

    public ExperimentSuite getExperiments() {
        Preconditions.checkState(experiments != null, "evaluation not in progress");
        return experiments;
    }

    public MeasurementSuite getMeasurements() {
        Preconditions.checkState(measurements != null, "evaluation not in progress");
        return measurements;
    }

    public ExperimentOutputLayout getOutputLayout() {
        Preconditions.checkState(layout != null, "evaluation not in progress");
        return layout;
    }

    public ExperimentOutputs getOutputs() {
        Preconditions.checkState(outputs != null, "evaluation not in progress");
        return outputs;
    }

    ExperimentSuite createExperimentSuite() {
        return new ExperimentSuite(algorithms, externalAlgorithms, dataSets);
    }

    MeasurementSuite createMeasurementSuite() {
        ImmutableList.Builder<MetricFactory> activeMetrics = ImmutableList.builder();
        activeMetrics.addAll(metrics);
        if (recommendOutputFile != null) {
            activeMetrics.add(new OutputTopNMetric.Factory());
        }
        if (predictOutputFile != null) {
            activeMetrics.add(new OutputPredictMetric.Factory(predictChannels));
        }
        return new MeasurementSuite(activeMetrics.build());
    }

    private void runEvaluations(DAGNode<JobGraph.Node, JobGraph.Edge> graph) throws TaskExecutionException, InterruptedException {
        int nthreads = getProject().getConfig().getThreadCount();
        TaskGraphExecutor exec;
        logger.info("Running evaluator with {} threads", nthreads);
        if (nthreads == 1) {
            exec = TaskGraphExecutor.singleThreaded();
        } else {
            exec = TaskGraphExecutor.create(nthreads);
        }

        try {
            exec.execute(graph);
        } catch (ExecutionException e) {
            Throwables.propagateIfInstanceOf(e.getCause(), TaskExecutionException.class);
            throw new TaskExecutionException("error in evaluation job task", e.getCause());
        }
    }

    DAGNode<JobGraph.Node,JobGraph.Edge> makeJobGraph(ExperimentSuite experiments) throws RecommenderConfigurationException {
        Multimap<UUID, TTDataSet> grouped = LinkedHashMultimap.create();
        for (TTDataSet dataset : experiments.getDataSets()) {
            UUID grp = dataset.getIsolationGroup();
            if (isolate) {
                // force isolation
                grp = UUID.randomUUID();
            }
            grouped.put(grp, dataset);
        }

        ComponentCache cache = new ComponentCache(cacheDir, getProject().getClassLoader());
        JobGraphBuilder builder = new JobGraphBuilder(this, cache);

        for (UUID groupId: grouped.keySet()) {
            Collection<TTDataSet> dss = grouped.get(groupId);
            String groupName = dss.size() == 1 ? dss.iterator().next().getName() : groupId.toString();
            for (TTDataSet dataset: dss) {
                // Add LensKit algorithms
                addAlgorithmNodes(builder, dataset, experiments.getAlgorithms(), cache);

                // Add external algorithms
                for (ExternalAlgorithm algo: experiments.getExternalAlgorithms()) {
                    builder.addExternalJob(algo, dataset);
                }
            }

            builder.fence(groupName);
        }
        return builder.getGraph();
    }

    private void addAlgorithmNodes(JobGraphBuilder builder, TTDataSet dataset,
                                   List<AlgorithmInstance> algorithms,
                                   ComponentCache cache) throws RecommenderConfigurationException {
        MergePool<Component,Dependency> pool = MergePool.create();

        for (AlgorithmInstance algo: algorithms) {
            logger.debug("building graph for algorithm {}", algo);
            LenskitConfiguration dataConfig = new LenskitConfiguration();
            ExecutionInfo info = ExecutionInfo.newBuilder()
                                              .setAlgorithm(algo)
                                              .setDataSet(dataset)
                                              .build();
            dataConfig.addComponent(info);
            dataset.configure(dataConfig);
            // Build the graph
            DAGNode<Component, Dependency> graph = algo.buildRecommenderGraph(dataConfig);

            if (!separateAlgorithms) {
                logger.debug("merging algorithm {} with previous graphs", algo);
                graph = pool.merge(graph);
            }
            if (cacheAll && cache != null) {
                cache.registerSharedNodes(graph.getReachableNodes());
            }

            builder.addLenskitJob(algo, dataset, graph);
        }
    }

    /**
     * Prepare the evaluation by opening all outputs and initializing metrics.
     */
    ExperimentOutputs openExperimentOutputs(ExperimentOutputLayout layouts, MeasurementSuite measures, TableWriter results, Closer closer) throws IOException {
        TableLayout resultLayout = layouts.getResultsLayout();
        TableWriter allResults = results;
        if (outputFile != null) {
            TableWriter disk = closer.register(CSVWriter.open(outputFile, resultLayout));
            allResults = new MultiplexedTableWriter(resultLayout, allResults, disk);
        }
        TableWriter user = null;
        if (userOutputFile != null) {
            user = closer.register(CSVWriter.open(userOutputFile, layouts.getUserLayout()));
        }
        List<Metric<?>> metrics = Lists.newArrayList();
        for (MetricFactory metric : measures.getMetricFactories()) {
            metrics.add(closer.register(metric.createMetric(this)));
        }
        return new ExperimentOutputs(layouts, allResults, user, metrics);
    }
}
