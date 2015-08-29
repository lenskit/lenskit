package org.lenskit.eval.traintest;

import org.grouplens.lenskit.util.table.Table;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Sets up and runs train-test evaluations.  This class can be used directly, but it will usually be controlled from
 * the `train-test` command line tool in turn driven by a Gradle script.  For a simpler way to programatically run an
 * evaluation, see {@link org.grouplens.lenskit.eval.traintest.SimpleEvaluator}, which provides a simplified interface
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
    private Path outputFile;
    private Path userOutputFile;

    private List<AlgorithmInstance> algorithms = new ArrayList<>();
    private List<DataSet> dataSets = new ArrayList<>();
    private List<EvalTask> tasks = new ArrayList<>();

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
     * Run the experiment.
     * @return The global aggregate results from the experiment.
     */
    public Table run() {
        throw new UnsupportedOperationException();
    }
}
