package org.lenskit.specs.eval;

import org.lenskit.specs.AbstractSpec;

import java.nio.file.Path;
import java.util.*;

/**
 * Specification for train-test experiments.
 */
public class TrainTestExperimentSpec extends AbstractSpec {
    private Path outputFile;
    private Path userOutputFile;
    private List<DataSetSpec> dataSets = new ArrayList<>();
    private List<AlgorithmSpec> algorithms = new ArrayList<>();
    private List<EvalTaskSpec> tasks = new ArrayList<>();

    /**
     * Get the global output file.
     * @return The global output file.
     */
    public Path getOutputFile() {
        return outputFile;
    }

    /**
     * Set the global output file.
     * @param file The output file for global aggregate measurements.
     */
    public void setOutputFile(Path file) {
        outputFile = file;
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
     * @param file The output file for per-user measurements.
     */
    public void setUserOutputFile(Path file) {
        userOutputFile = file;
    }

    /**
     * Get all output files for this experiment.
     * @return A set of all known output files.
     */
    public Set<Path> getOutputFiles() {
        Set<Path> files = new HashSet<>();
        if (outputFile != null) {
            files.add(outputFile);
        }
        if (userOutputFile != null) {
            files.add(userOutputFile);
        }
        for (EvalTaskSpec task: tasks) {
            files.addAll(task.getOutputFiles());
        }
        return files;
    }

    /**
     * Get the list of data sets.
     * @return The list of data sets.
     */
    public List<DataSetSpec> getDataSets() {
        return dataSets;
    }

    /**
     * Set the list of data sets.
     * @param dss The list of data sets to use.
     */
    public void setDataSets(List<DataSetSpec> dss) {
        dataSets = dss;
    }

    /**
     * Add a data set.
     * @param ds A data set to use.
     */
    public void addDataSet(DataSetSpec ds) {
        dataSets.add(ds);
    }

    /**
     * Add some data sets.
     * @param dss Data sets to add.
     */
    public void addDataSets(Collection<? extends DataSetSpec> dss) {
        dataSets.addAll(dss);
    }

    /**
     * Add some data sets.
     * @param dss Data sets to add.
     */
    public void addDataSets(DataSetSpec... dss) {
        dataSets.addAll(Arrays.asList(dss));
    }

    /**
     * Get the list of algorithms.
     * @return The list of algorithms.
     */
    public List<AlgorithmSpec> getAlgorithms() {
        return algorithms;
    }

    /**
     * Set the list of algorithms.
     * @param dss The list of algorithms to use.
     */
    public void setAlgorithms(List<AlgorithmSpec> dss) {
        algorithms = dss;
    }

    /**
     * Add a algorithm.
     * @param ds A algorithm to use.
     */
    public void addAlgorithm(AlgorithmSpec ds) {
        algorithms.add(ds);
    }

    /**
     * Add some algorithms.
     * @param dss Algorithms to add.
     */
    public void addAlgorithms(Collection<? extends AlgorithmSpec> dss) {
        algorithms.addAll(dss);
    }

    /**
     * Add some algorithms.
     * @param dss Algorithms to add.
     */
    public void addAlgorithms(AlgorithmSpec... dss) {
        algorithms.addAll(Arrays.asList(dss));
    }

    /**
     * Get the list of tasks.
     * @return The list of eval tasks.
     */
    public List<EvalTaskSpec> getTasks() {
        return tasks;
    }

    /**
     * Set the list of eval tasks.
     * @param tasks The list of eval tasks to use.
     */
    public void setTasks(List<EvalTaskSpec> tasks) {
        this.tasks = tasks;
    }

    /**
     * Add a evaltask.
     * @param ds A evaltask to use.
     */
    public void addTask(EvalTaskSpec ds) {
        tasks.add(ds);
    }

    /**
     * Add some evaltasks.
     * @param dss Evaltasks to add.
     */
    public void addTasks(Collection<? extends EvalTaskSpec> dss) {
        tasks.addAll(dss);
    }

    /**
     * Add some evaltasks.
     * @param dss Evaltasks to add.
     */
    public void addTasks(EvalTaskSpec... dss) {
        tasks.addAll(Arrays.asList(dss));
    }
}
