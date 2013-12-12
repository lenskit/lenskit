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
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.DAGNodeBuilder;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.core.RecommenderConfigurationException;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.eval.AbstractTask;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstanceBuilder;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.Metric;
import org.grouplens.lenskit.eval.metrics.TestUserMetric;
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
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
    private List<TestUserMetric> metrics;
    private List<ModelMetric> modelMetrics;
    private List<Pair<Symbol,String>> predictChannels;
    private boolean isolate;
    private File outputFile;
    private File userOutputFile;
    private File predictOutputFile;
    private File recommendOutputFile;

    private ExperimentSuite experiments;
    private MeasurementSuite measurements;
    private ExperimentOutputLayout layout;

    public TrainTestEvalTask() {
        this("train-test");
    }

    public TrainTestEvalTask(String name) {
        super(name);
        dataSets = Lists.newArrayList();
        algorithms = Lists.newArrayList();
        externalAlgorithms = Lists.newArrayList();
        metrics = Lists.newArrayList();
        modelMetrics = Lists.newArrayList();
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

    public TrainTestEvalTask setAlgorithm(Map<String,Object> attrs, String file) throws IOException, RecommenderConfigurationException {
        algorithms.add(new AlgorithmInstanceBuilder().configureFromFile(attrs, new File(file))
                                                            .build());
        return this;
    }

    public TrainTestEvalTask addExternalAlgorithm(ExternalAlgorithm algorithm) {
        externalAlgorithms.add(algorithm);
        return this;
    }

    public TrainTestEvalTask addMetric(TestUserMetric metric) {
        metrics.add(metric);
        return this;
    }

    public TrainTestEvalTask addMetric(Class<? extends TestUserMetric> metricClass) throws IllegalAccessException, InstantiationException {
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
        modelMetrics.add(new FunctionMultiModelMetric(file, columns, metric));
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
        modelMetrics.add(new FunctionModelMetric(columns, metric));
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
     * Control whether the train-test evaluator will isolate data sets.  If set to {@code true},
     * then each data set will be run in turn, with no inter-data-set parallelism.  This can
     * reduce memory usage for some large runs, keeping the data from only a single data set in
     * memory at a time.  Otherwise (the default), individual algorithmInfo/data-set runs may be freely
     * intermingled.
     *
     * @param iso Whether to isolate data sets.
     * @return The task (for chaining).
     */
    public TrainTestEvalTask setIsolate(boolean iso) {
        isolate = iso;
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

    List<TestUserMetric> getMetrics() {
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
     * Run the evaluation on the train test data source files
     *
     * @return The summary output table.
     * @throws org.grouplens.lenskit.eval.TaskExecutionException
     *          Failure of the evaluation
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Table perform() throws TaskExecutionException {
        try {
            experiments = createExperimentSuite();
            measurements = createMeasurementSuite();
            layout = ExperimentOutputLayout.create(experiments, measurements);
            TableBuilder resultsBuilder = new TableBuilder(layout.getResultsLayout());

            logger.info("Starting evaluation");
            Closer closer = Closer.create();

            try {
                ExperimentOutputs outputs = openExperimentOutputs(layout, resultsBuilder, closer);
                DAGNode<TaskGraph.Node,TaskGraph.Edge> jobGraph =
                        makeJobGraph(experiments, measurements, outputs);
                // tell all metrics to get started
                for (Metric<TrainTestEvalTask> metric : measurements.getAllMetrics()) {
                    metric.startEvaluation(this);
                }
                try {
                    runEvaluations(jobGraph);
                } finally {
                    for (Metric<TrainTestEvalTask> metric : measurements.getAllMetrics()) {
                        metric.finishEvaluation();
                    }
                }
            } catch (Throwable th) {
                throw closer.rethrow(th, TaskExecutionException.class);
            } finally {
                closer.close();
            }

            return resultsBuilder.build();
        } catch (IOException e) {
            throw new TaskExecutionException("I/O error", e);
        } finally {
            experiments = null;
            measurements = null;
            layout = null;
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

    private ExperimentSuite createExperimentSuite() {
        return new ExperimentSuite(algorithms, externalAlgorithms, dataSets);
    }

    private MeasurementSuite createMeasurementSuite() {
        return new MeasurementSuite(metrics, modelMetrics, predictChannels);
    }

    private void runEvaluations(DAGNode<TaskGraph.Node, TaskGraph.Edge> graph) throws TaskExecutionException {
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

    DAGNode<TaskGraph.Node,TaskGraph.Edge> makeJobGraph(ExperimentSuite experiments, MeasurementSuite measurements, ExperimentOutputs outputs) {
        DAGNode<TaskGraph.Node,TaskGraph.Edge> graph = null;
        DAGNodeBuilder<TaskGraph.Node,TaskGraph.Edge> builder =
                DAGNode.newBuilder(TaskGraph.groupNode());
        for (TTDataSet dataset : experiments.getDataSets()) {
            final Provider<PreferenceSnapshot> snap = SharedPreferenceSnapshot.provider(dataset);
            // Add LensKit algorithms
            for (AlgorithmInstance algo: experiments.getAlgorithms()) {
                TrainTestJob job = new LenskitEvalJob(algo, dataset, measurements,
                                                      outputs.getPrefixed(algo, dataset),
                                                      snap);
                DAGNodeBuilder<TaskGraph.Node, TaskGraph.Edge> nb = DAGNode.newBuilder();
                nb.setLabel(TaskGraph.jobNode(job));
                if (graph != null) {
                    nb.addEdge(graph, TaskGraph.edge());
                }
                builder.addEdge(nb.build(), TaskGraph.edge());
            }

            // Add external algorithms
            for (ExternalAlgorithm algo: experiments.getExternalAlgorithms()) {
                TrainTestJob job = new ExternalEvalJob(algo, dataset, measurements,
                                                       outputs.getPrefixed(algo, dataset));
                DAGNode<TaskGraph.Node, TaskGraph.Edge> node =
                        DAGNode.singleton(TaskGraph.jobNode(job));
                builder.addEdge(node, TaskGraph.edge());
            }

            // Use dependencies to encode data set isolation
            if (isolate) {
                graph = builder.build();
                builder = DAGNode.newBuilder();
                builder.setLabel(TaskGraph.groupNode());
            }
        }
        if (graph == null) {
            assert !isolate;
            graph = builder.build();
        }
        return graph;
    }

    /**
     * Prepare the evaluation by opening all outputs and initializing metrics.
     */
    private ExperimentOutputs openExperimentOutputs(ExperimentOutputLayout layouts, TableWriter results, Closer closer) throws IOException {
        logger.info("Starting evaluation");
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
        TableWriter predict = null;
        if (predictOutputFile != null) {
            predict = closer.register(CSVWriter.open(predictOutputFile, layouts.getPredictLayout()));
        }
        TableWriter recommend = null;
        if (recommendOutputFile != null) {
            recommend = closer.register(CSVWriter.open(recommendOutputFile, layouts.getRecommendLayout()));
        }
        return new ExperimentOutputs(layouts, allResults, user, predict, recommend);
    }
}
