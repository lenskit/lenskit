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
package org.lenskit.eval.traintest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.google.common.io.Closer;
import com.google.common.util.concurrent.UncheckedExecutionException;
import groovy.lang.Closure;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.MergePool;
import org.grouplens.grapht.util.ClassLoaders;
import org.lenskit.LenskitConfiguration;
import org.lenskit.config.ConfigHelpers;
import org.lenskit.eval.traintest.predict.PredictEvalTask;
import org.lenskit.eval.traintest.recommend.RecommendEvalTask;
import org.lenskit.util.io.CompressionMode;
import org.lenskit.util.io.LKFileUtils;
import org.lenskit.util.monitor.TrackedJob;
import org.lenskit.util.parallel.TaskGroup;
import org.lenskit.util.table.Table;
import org.lenskit.util.table.TableBuilder;
import org.lenskit.util.table.TableLayout;
import org.lenskit.util.table.TableLayoutBuilder;
import org.lenskit.util.table.writer.CSVWriter;
import org.lenskit.util.table.writer.MultiplexedTableWriter;
import org.lenskit.util.table.writer.TableWriter;
import org.lenskit.util.table.writer.TableWriters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;

/**
 * Sets up and runs train-test evaluations.  This class can be used directly, but it will usually be controlled from
 * the `train-test` command line tool in turn driven by a Gradle script.  For a simpler way to programatically run an
 * evaluation, see {@link org.lenskit.eval.traintest.SimpleEvaluator}, which provides a simplified interface
 * to train-test evaluations with cross-validation.
 *
 * A train-test experiment experiment consists of three things:
 *
 * - A collection of algorithms.
 * - A collection of train-test data sets.
 * - A collection of tasks, each of which performs an action on the recommender (e.g. predict users' test
 * ratings, or produce recommendations) and measures the recommender's performance on that task using one
 * or more metrics.
 *
 * Global output is aggregated into a CSV file; individual tasks or metrics may produce additional files.
 */
public class TrainTestExperiment {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestExperiment.class);
    /**
     * The job type code for train-test experiments
     * @see TrackedJob#getType()
     */
    public static final String JOB_TYPE = "train-test";

    private Path outputFile;
    private Path userOutputFile;
    private Path cacheDir;
    private boolean shareModelComponents = true;
    private int threadCount = 0;
    private int parallelTasks = 0;
    private boolean continueAfterError = false;
    private ClassLoader classLoader = ClassLoaders.inferDefault(TrainTestExperiment.class);

    private List<AlgorithmInstance> algorithms = new ArrayList<>();
    private List<DataSet> dataSets = new ArrayList<>();
    private List<EvalTask> tasks = new ArrayList<>();

    private TableWriter globalOutput;
    private TableWriter userOutput;
    private TableBuilder resultBuilder;
    private Closer resultCloser;
    private ExperimentOutputLayout outputLayout;
    private List<ExperimentJob> allJobs;
    private TaskGroup rootJob;

    /**
     * Set the primary output file.
     * @param out The file where the primary aggregate output should go.
     */
    public void setOutputFile(Path out) {
        outputFile = out;
    }

    /**
     * Get the primary output file.
     * @return The primary output file.
     */
    public Path getOutputFile() {
        return outputFile;
    }

    /**
     * Get the per-user output file.
     * @return The output file for per-user measurements.
     */
    public Path getUserOutputFile() {
        return userOutputFile;
    }

    /**
     * Set the per-user output file.
     * @param file The file for per-user measurements.
     */
    public void setUserOutputFile(Path file) {
        userOutputFile = file;
    }

    /**
     * Get the algorithm instances.
     * @return The algorithms to run.
     */
    public List<AlgorithmInstance> getAlgorithms() {
        return algorithms;
    }

    /**
     * Add an algorithm to the experiment.
     * @param algo The algorithm to add.
     */
    public void addAlgorithm(AlgorithmInstance algo) {
        algorithms.add(algo);
    }

    /**
     * Add multiple algorithm instances.
     * @param algos The algorithm instances to add.
     */
    public void addAlgorithms(List<AlgorithmInstance> algos) {
        algorithms.addAll(algos);
    }

    /**
     * Add an algorithm configured by a Groovy closure.  Mostly useful for testing.
     * @param name The algorithm name.
     * @param block The algorithm configuration block.
     */
    public void addAlgorithm(String name, Closure<?> block) {
        AlgorithmInstanceBuilder aib = new AlgorithmInstanceBuilder(name);
        LenskitConfiguration config = aib.getConfig();
        ConfigHelpers.configure(config, block);
        addAlgorithm(aib.build());
    }

    /**
     * Add one or more algorithms by loading a config file.
     * @param name The algorithm name.
     * @param file The config file to load.
     */
    public void addAlgorithm(String name, Path file) {
        addAlgorithms(AlgorithmInstance.load(file, name, classLoader));
    }

    /**
     * Add one or more algorithms from a configuration file.
     * @param file The configuration file.
     */
    public void addAlgorithms(Path file) {
        addAlgorithm(null, file);
    }

    /**
     * Get the list of data sets to use.
     * @return The list of data sets to use.
     */
    public List<DataSet> getDataSets() {
        return dataSets;
    }

    /**
     * Add a data set.
     * @param ds The data set to add.
     */
    public void addDataSet(DataSet ds) {
        dataSets.add(ds);
    }

    /**
     * Add several data sets.
     * @param dss The data sets to add.
     */
    public void addDataSets(List<DataSet> dss) {
        dataSets.addAll(dss);
    }

    /**
     * Query whether this experiment will cache and share components.
     *
     * @return {@code true} if model components will be shared.
     * @see #setShareModelComponents(boolean)
     */
    public boolean getShareModelComponents() {
        return shareModelComponents;
    }

    /**
     * Control whether model components will be shared.  If {@link #setCacheDirectory(Path)} is also set,
     * components will be cached on disk; otherwise, they will be opportunistically shared in memory.
     *
     * Cached output improves throughput and memory use, but makes build times effectively meaningless.  It
     * is turned on by default, but turn it off if you want to measure recommender build times.
     *
     * @param shares `true` to enable caching of shared model components.
     */
    public void setShareModelComponents(boolean shares) {
        shareModelComponents = shares;
    }

    /**
     * Get the cache directory for model components.
     * @return The directory where model components will be cached.
     */
    public Path getCacheDirectory() {
        return cacheDir;
    }

    /**
     * Set the cache directory for model components.
     * @param dir The directory where model components will be cached.
     */
    public void setCacheDirectory(Path dir) {
        cacheDir = dir;
    }

    /**
     * Get the number of threads that the experiment may use.
     *
     * @return The number of threads that the experiment may use.
     */
    public int getThreadCount() {
        int tc = threadCount;
        if (tc <= 0) {
            String prop = System.getProperty("lenskit.eval.threadCount");
            if (prop != null) {
                tc = Integer.parseInt(prop);
            }
        }
        if (tc <= 0) {
            tc = Runtime.getRuntime().availableProcessors();
        }
        return tc;
    }

    /**
     * Set the number of threads the experiment may use.
     *
     * @param tc The number of threads that the experiment may use.  If 0 (the default), consults the property
     *           `lenskit.eval.threadCount`, and if that is unset, uses as many threads as there
     *           are available processors according to {@link Runtime#availableProcessors()}.
     */
    public void setThreadCount(int tc) {
        threadCount = tc;
    }

    /**
     * Get the number of evaluation tasks to permit to run in parallel.  Reducing this can be useful for reducing
     * the memory use of LensKit.
     *
     * @return The number of evaluation tasks to allow to run in parallel, or 0 if there is no limit.
     */
    public int getParallelTasks() {
        return parallelTasks;
    }

    /**
     * Set the number of parallel experiment tasks to run.  If 0 (the default), then up to {@link #getThreadCount()}
     * jobs can run in parallel.  This can be reduced to reduce memory use (by having fewer models in memory); the
     * extra threads may still be used to speed up individual evaluations.
     *
     * @param pt The number of tasks to run in parallel, or 0 to have no limit.
     */
    public void setParallelTasks(int pt) {
        parallelTasks = pt;
    }

    /**
     * Query whether this task will continue in the face of an error.
     * @return `true` if the experiment will keep going if a segment fails.
     */
    public boolean getContinueAfterError() {
        return continueAfterError;
    }

    /**
     * Configure whether the experiment will continue after an error.
     * @param c `true` to continue after an error.
     */
    public void setContinueAfterError(boolean c) {
        continueAfterError = c;
    }

    /**
     * Get the class loader for this experiment.
     * @return The class loader that will be used.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Set the class loader for this experiment.
     * @param loader The class loader to use.
     */
    public void setClassLoader(ClassLoader loader) {
        classLoader = loader;
    }

    /**
     * Get the eval tasks to be used in this experiment.
     * @return The evaluation tasks to run.
     */
    public List<EvalTask> getTasks() {
        return tasks;
    }

    /**
     * Add an evaluation task.
     * @param task An evaluation task to run.
     */
    public void addTask(EvalTask task) {
        tasks.add(task);
    }

    /**
     * Convenience method to get the prediction task for the experiment.  If there is not yet a prediction task, then
     * one is added.
     * @return The experiment's prediction task.
     */
    PredictEvalTask getPredictionTask() {
        List<PredictEvalTask> taskList = FluentIterable.from(tasks)
                                                   .filter(PredictEvalTask.class)
                                                   .toList();
        if (taskList.isEmpty()) {
            PredictEvalTask task = new PredictEvalTask();
            addTask(task);
            return task;
        } else {
            if (taskList.size() > 1) {
                logger.warn("multiple prediction tasks configured");
            }
            return taskList.get(0);
        }
    }

    /**
     * Get the global output table.
     * @return The global output table.
     */
    @Nonnull
    TableWriter getGlobalOutput() {
        Preconditions.checkState(resultBuilder != null, "Experiment has not been started");
        assert globalOutput != null;
        return globalOutput;
    }

    /**
     * Get the per-user output table.
     * @return The per-user output table.
     */
    @Nonnull
    TableWriter getUserOutput() {
        Preconditions.checkState(resultBuilder != null, "Experiment has not been started");
        return userOutput;
    }

    /**
     * Run the experiment.
     * @return The global aggregate results from the experiment.
     */
    public Table execute() {
        try {
            try {
                resultCloser = Closer.create();
                logger.debug("setting up output");
                ExperimentOutputLayout layout = makeExperimentOutputLayout();
                openOutputs(layout);
                for (EvalTask task: tasks) {
                    task.start(layout);
                }

                logger.debug("gathering jobs");
                buildJobGraph();
                int nthreads = getThreadCount();
                if (nthreads > 1) {
                    logger.info("running with {} threads", nthreads);
                    runJobGraph(nthreads);
                } else {
                    logger.info("running in a single thread");
                    runJobList();
                }

                logger.info("train-test evaluation complete");
                // done before closing, but that is ok
                return resultBuilder.build();
            } catch (Throwable th) { //NOSONAR using closer
                throw resultCloser.rethrow(th);
            } finally {
                outputLayout = null;
                // FIXME Handle exceptions in task shutdown cleanly
                for (EvalTask task: tasks) {
                    task.finish();
                }
                resultBuilder = null;
                resultCloser.close();
            }
        } catch (IOException ex) {
            throw new EvaluationException("I/O error in evaluation", ex);
        }
    }

    public ExperimentOutputLayout getOutputLayout() {
        Preconditions.checkState(outputLayout != null, "experiment is not started");
        return outputLayout;
    }

    private ExperimentOutputLayout makeExperimentOutputLayout() {
        Set<String> dataColumns = Sets.newLinkedHashSet();
        Set<String> algoColumns = Sets.newLinkedHashSet();
        for (DataSet ds: getDataSets()) {
            dataColumns.addAll(ds.getAttributes().keySet());
        }
        for (AlgorithmInstance ai: getAlgorithms()) {
            algoColumns.addAll(ai.getAttributes().keySet());
        }
        return new ExperimentOutputLayout(dataColumns, algoColumns);
    }

    private void openOutputs(ExperimentOutputLayout eol) throws IOException {
        TableLayout globalLayout = makeGlobalResultLayout(eol);
        resultBuilder = resultCloser.register(new TableBuilder(globalLayout));
        if (outputFile != null) {
            TableWriter csvw = resultCloser.register(CSVWriter.open(outputFile.toFile(), globalLayout, CompressionMode.AUTO));
            globalOutput = resultCloser.register(new MultiplexedTableWriter(globalLayout, resultBuilder, csvw));
        } else {
            globalOutput = resultBuilder;
        }

        TableLayout ul = makeUserResultLayout(eol);
        if (userOutputFile != null) {
            userOutput = resultCloser.register(CSVWriter.open(userOutputFile.toFile(), ul, CompressionMode.AUTO));
        } else {
            userOutput = TableWriters.noop(ul);
        }
        outputLayout = eol;
    }

    private TableLayout makeGlobalResultLayout(ExperimentOutputLayout eol) {
        TableLayoutBuilder tlb = TableLayoutBuilder.copy(eol.getConditionLayout());
        tlb.addColumn("Succeeded")
           .addColumn("BuildTime")
           .addColumn("TestTime");
        for (EvalTask task: tasks) {
            tlb.addColumns(task.getGlobalColumns());
        }
        return tlb.build();
    }

    private TableLayout makeUserResultLayout(ExperimentOutputLayout eol) {
        TableLayoutBuilder tlb = TableLayoutBuilder.copy(eol.getConditionLayout());
        tlb.addColumn("User")
           .addColumn("TestTime")
           .addColumn("TestItems");
        for (EvalTask task: tasks) {
            tlb.addColumns(task.getUserColumns());
        }
        return tlb.build();
    }


    /**
     * Create the tree of jobs to run in this experiment.
     */
    @Nonnull
    private void buildJobGraph() {
        allJobs = new ArrayList<>();
        TrackedJob tracker = new TrackedJob(JOB_TYPE);
        tracker.getEventBus().register(new StatusLogger(logger));
        ComponentCache cache = null;
        if (shareModelComponents) {
            cache = new ComponentCache(cacheDir, classLoader);
        }
        Map<UUID,TaskGroup> groups = new HashMap<>();
        Semaphore limit = null;
        if (parallelTasks > 0) {
            limit = new Semaphore(parallelTasks);
        }

        // set up the roots
        LenskitConfiguration config = new LenskitConfiguration();
        for (EvalTask task: tasks) {
            for (Class<?> cls: task.getRequiredRoots()) {
                config.addRoot(cls);
            }
        }

        // make tasks
        for (DataSet ds: getDataSets()) {
            // TODO support global isolation
            UUID gid = ds.getIsolationGroup();
            TaskGroup group = groups.get(gid);
            if (group == null) {
                group = new TaskGroup(true);
                groups.put(gid, group);
                group.setContinueAterError(continueAfterError);
            }
            MergePool<Component,Dependency> pool = null;
            if (cache != null) {
                pool = MergePool.create();
            }
            for (AlgorithmInstance ai: getAlgorithms()) {
                TrackedJob j = tracker.makeChild(ExperimentJob.JOB_TYPE, "evaluate " + ai + " on " + ds);
                ExperimentJob job = new ExperimentJob(this, ai, ds, config, cache, pool, j, limit);
                allJobs.add(job);
                group.addTask(job);
            }
        }

        TaskGroup root;
        if (groups.size() > 1) {
            root = new TaskGroup(false);
            root.setContinueAterError(continueAfterError);
            for (TaskGroup g: groups.values()) {
                root.addTask(g);
            }
        } else {
            root = FluentIterable.from(groups.values()).first().orNull();
        }

        Preconditions.checkState(root != null, "no jobs defined");
        rootJob = root;
    }

    /**
     * Run the jobs in sequence.
     */
    private void runJobList() {
        Preconditions.checkState(allJobs != null, "job graph not built");

        try {
            rootJob.compute();
        } catch (Throwable err) {
            logger.error("experiment failed", err);
            if (!continueAfterError) {
                Throwables.throwIfUnchecked(err);
                // will only happen if an undeclared checked exception slips through
                throw new UncheckedExecutionException(err);
            }
        }
    }

    private void runJobGraph(int nthreads) {
        Preconditions.checkState(rootJob != null, "job graph not built");
        ForkJoinPool pool = new ForkJoinPool(nthreads);
        try {
            pool.invoke(rootJob);
        } catch (Throwable err) {
            logger.error("experiment failed", err);
            if (!continueAfterError) {
                Throwables.throwIfUnchecked(err);
                // will only happen if an undeclared checked exception slips through
                throw new UncheckedExecutionException(err);
            }
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Load a train-test experiment from a YAML file.
     * @param file The file to load.
     * @return The train-test experiment.
     */
    public static TrainTestExperiment load(Path file) throws IOException {
        YAMLFactory factory = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode node = mapper.readTree(file.toFile());

        return fromJSON(node, file.toUri());
    }

    /**
     * Configure a train-test experiment from JSON.
     * @param json The JSON node.
     * @param base The base URI for resolving relative paths.
     * @return The train-test experiment.
     * @throws IOException if there is an IO error.
     */
    static TrainTestExperiment fromJSON(JsonNode json, URI base) throws IOException {
        TrainTestExperiment exp = new TrainTestExperiment();

        // configure basic settings
        String outFile = json.path("output_file").asText(null);
        if (outFile != null) {
            exp.setOutputFile(Paths.get(base.resolve(outFile)));
        }
        outFile = json.path("user_output_file").asText(null);
        if (outFile != null) {
            exp.setUserOutputFile(Paths.get(base.resolve(outFile)));
        }
        String cacheDir = json.path("cache_directory").asText(null);
        if (cacheDir != null) {
            exp.setCacheDirectory(Paths.get(base.resolve(cacheDir)));
        }
        if (json.has("thread_count")) {
            exp.setThreadCount(json.get("thread_count").asInt(1));
        }
        exp.setParallelTasks(json.path("parallel_tasks").asInt(0));
        if (json.has("share_model_components")) {
            exp.setShareModelComponents(json.get("share_model_components").asBoolean());
        }
        exp.setContinueAfterError(json.path("continue_after_error").asBoolean(false));
        if (!json.has("datasets")) {
            throw new IllegalArgumentException("no data sets specified");
        }

        // configure data sets
        for (JsonNode ds: json.get("datasets")) {
            List<DataSet> dss;
            if (ds.isTextual()) {
                URI dsURI = base.resolve(ds.asText());
                dss = DataSet.load(dsURI.toURL());
            } else {
                dss = DataSet.fromJSON(ds, base);
            }
            exp.addDataSets(dss);
        }

        // configure the algorithms
        JsonNode algo = json.path("algorithms");
        if (algo.isTextual()) {
            // name of groovy file
            URI af = base.resolve(algo.asText());
            String aname = LKFileUtils.basename(af.getPath(), false);
            // FIXME Support algorithms from URLs
            exp.addAlgorithm(aname, Paths.get(af));
        } else if (algo.isObject()) {
            // mapping of names to groovy files
            Iterator<Map.Entry<String,JsonNode>> algoIter = algo.fields();
            while (algoIter.hasNext()) {
                Map.Entry<String, JsonNode> e = algoIter.next();
                URI algoUri = base.resolve(e.getValue().asText());
                // FIXME Support algorithms from URLs
                exp.addAlgorithm(e.getKey(), Paths.get(algoUri));
            }
        } else if (algo.isArray()) {
            // list of groovy file names
            for (JsonNode an: algo) {
                URI af = base.resolve(an.asText());
                String aname = LKFileUtils.basename(af.getPath(), false);
                // FIXME Support algorithms from URLs
                exp.addAlgorithm(aname, Paths.get(af));
            }
        } else if (!algo.isMissingNode()) {
            throw new IllegalArgumentException("unexpected type for algorithms config");
        }

        // configure the tasks and their metrics
        JsonNode tasks = json.get("tasks");
        for (JsonNode task: tasks) {
            exp.addTask(configureTask(task, base));
        }

        return exp;
    }

    private static EvalTask configureTask(JsonNode task, URI base) throws IOException {
        String type = task.path("type").asText(null);
        Preconditions.checkArgument(type != null, "no task type specified");
        switch (type) {
        case "predict":
            return PredictEvalTask.fromJSON(task, base);
        case "recommend":
            return RecommendEvalTask.fromJSON(task, base);
        default:
            throw new IllegalArgumentException("invalid eval task type " + type);
        }
    }
}
