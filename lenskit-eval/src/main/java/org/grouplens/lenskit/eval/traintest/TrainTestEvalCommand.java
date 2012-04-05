package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.io.Closeables;
import org.grouplens.lenskit.eval.*;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.TestUserMetric;
import org.grouplens.lenskit.util.tablewriter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
/**
 * The command that run the algorithm instance and output the prediction result file and the evaluation result file
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public class TrainTestEvalCommand extends AbstractCommand<Void> {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestEvalCommand.class);
    
    private List<TTDataSet> dataSources;
    private List<AlgorithmInstance> algorithms;
    private List<TestUserMetric> metrics;
    private IsolationLevel isolationLevel;
    private int nThread;
    private File outputFile;
    private File userOutputFile;
    private File predictOutputFile;
    // default value for recommendation set size
    private int numRecs = 5;

    private int commonColumnCount;
    private TableLayout outputLayout;
    private TableLayout userLayout;
    private TableLayout predictLayout;

    private TableWriter output;
    private TableWriter userOutput;
    private TableWriter predictOutput;

    private List<JobGroup> jobGroups;
    private Map<String, Integer> dataColumns;
    private Map<String, Integer> algoColumns;
    private List<TestUserMetric> predictMetrics;
    

    public TrainTestEvalCommand() {
        this("Traintest");
    }

    public TrainTestEvalCommand(String name) {
        super(name);
        dataSources = new LinkedList<TTDataSet>();
        algorithms = new LinkedList<AlgorithmInstance>();
        metrics = new LinkedList<TestUserMetric>();
        outputFile = new File("train-test-results.csv");
        isolationLevel = IsolationLevel.NONE;
    }

    public TrainTestEvalCommand addDataset(TTDataSet source) {
        dataSources.add(source);
        return this;
    }

    public TrainTestEvalCommand addAlgorithm(AlgorithmInstance algorithm) {
        algorithms.add(algorithm);
        return this;
    }

    public TrainTestEvalCommand addMetric(TestUserMetric metric) {
        metrics.add(metric);
        return this;
    }

    public TrainTestEvalCommand setOutput(File file) {
        outputFile = file;
        return this;
    }

    public TrainTestEvalCommand setUserOutput(File file) {
        userOutputFile = file;
        return this;
    }

    public TrainTestEvalCommand setPredictOutput(File file) {
        predictOutputFile = file;
        return this;
    }

    public TrainTestEvalCommand setIsolation(IsolationLevel level) {
        isolationLevel = level;
        return this;
    }
    
    public TrainTestEvalCommand setThread(int n) {
        nThread = n;
        return this;
    }

    List<TTDataSet> dataSources() {
        return dataSources;
    }

    List<AlgorithmInstance> getAlgorithms() {
        return algorithms;
    }

    List<TestUserMetric> getMetrics() {
        return metrics;
    }

    File getOutput() {
        return outputFile;
    }

    File getPredictOutput() {
        return predictOutputFile;
    }

    public int getNumRecs() {
        return numRecs;
    }

    public TrainTestEvalCommand setNumRecs(int numRecs) {
        this.numRecs = numRecs;
        return this;
    }

    /**
     * Run the evaluation on the train test data source files
     *
     * @return For now, return nothing
     * @throws CommandFailedException  Failure of the evaluation
     */
    @Override
    public Void call() throws CommandFailedException {
        this.setupJobs();
        int nthreads = nThread;
        if (nthreads <= 0) {
            nthreads = Runtime.getRuntime().availableProcessors();
        }
        logger.info("Starting evaluation");
        this.start();
        logger.info("Running evaluator with {} threads", nthreads);
        JobGroupExecutor exec;
        switch (isolationLevel) {
            case NONE:
                exec = new MergedJobGroupExecutor(nthreads);
                break;
            case JOB_GROUP:
                exec = new SequentialJobGroupExecutor(nthreads);
                break;
            default:
                throw new RuntimeException("Invalid isolation level " + isolationLevel);
        }

        for (JobGroup group: this.getJobGroups()) {
            exec.add(group);
        }
        try {
            exec.run();
        } catch (ExecutionException e) {
            throw new CommandFailedException("Error running the evaluation", e);
        } finally {
            logger.info("Finishing evaluation");
            this.finish();
        }
        return null;
    }

    protected void setupJobs() {
        TableLayoutBuilder master = new TableLayoutBuilder();
        master.addColumn("Algorithm");
        dataColumns = new HashMap<String, Integer>();
        for (TTDataSet ds: dataSources) {
            for (String attr: ds.getAttributes().keySet()) {
                if (!dataColumns.containsKey(attr)) {
                    dataColumns.put(attr, master.addColumn(attr));
                }
            }
        }

        algoColumns = new HashMap<String, Integer>();
        for (AlgorithmInstance algo: algorithms) {
            for (String attr: algo.getAttributes().keySet()) {
                if (!algoColumns.containsKey(attr)) {
                    algoColumns.put(attr, master.addColumn(attr));
                }
            }
        }

        jobGroups = new ArrayList<JobGroup>(dataSources.size());
        for (TTDataSet dataset: dataSources) {
            TrainTestEvalJobGroup group;
            group = new TrainTestEvalJobGroup(this, algorithms, metrics, dataset, numRecs);
            jobGroups.add(group);
        }

        commonColumnCount = master.getColumnCount();

        TableLayoutBuilder output = master.clone();
        output.addColumn("BuildTime");
        output.addColumn("TestTime");
        TableLayoutBuilder perUser = master.clone();

        String[] columnLabels;
        String[] userColumnLabels;

        for (TestUserMetric ev: metrics) {
            columnLabels = ev.getColumnLabels();
            if (columnLabels != null){
                for (String c: columnLabels) {
                    output.addColumn(c);
                }
            }

            userColumnLabels = ev.getUserColumnLabels();
            if (userColumnLabels != null){
                for (String c: userColumnLabels) {
                    perUser.addColumn(c);
                }
            }
        }

        outputLayout = output.build();
        userLayout = perUser.build();

        TableLayoutBuilder eachPred = master.clone();
        eachPred.addColumn("User");
        eachPred.addColumn("Item");
        eachPred.addColumn("Rating");
        eachPred.addColumn("Prediction");

        predictLayout = eachPred.build();

        predictMetrics = metrics;
    }

    public void start() {
        logger.info("Starting evaluation");
        try {
            output = CSVWriter.open(outputFile, outputLayout);
        } catch (IOException e) {
            throw new RuntimeException("Error opening output table", e);
        }
        if (userOutputFile != null) {
            try {
                userOutput = CSVWriter.open(userOutputFile, userLayout);
            } catch (IOException e) {
                Closeables.closeQuietly(output);
                throw new RuntimeException("Error opening user output table", e);
            }
        }
        if (predictOutputFile != null) {
            try {
                predictOutput = CSVWriter.open(predictOutputFile, predictLayout);
            } catch (IOException e) {
                Closeables.closeQuietly(userOutput);
                Closeables.closeQuietly(output);
                throw new RuntimeException("Error opening prediction table", e);
            }
        }
        for (TestUserMetric metric: predictMetrics) {
            metric.startEvaluation(this);
        }
    }

    public void finish() {
        for (TestUserMetric metric: predictMetrics) {
            metric.finishEvaluation();
        }
        if (output == null) {
            throw new IllegalStateException("evaluation not running");
        }
        logger.info("Evaluation finished");
        try {
            // FIXME Catch all exceptions closing output
            output.close();
            if (userOutput != null) {
                userOutput.close();
            }
            if (predictOutput != null) {
                predictOutput.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error closing output", e);
        } finally {
            output = null;
        }
    }

    /**
     * Get the evaluation's output table. Used by job groups to set up the
     * output for their jobs.
     *
     * @return A supplier for the table writer for this evaluation.
     * @throws IllegalStateException if the job has not been started or is
     *         finished.
     */
    @Nonnull
    Supplier<TableWriter> outputTableSupplier() {
        return new Supplier<TableWriter>() {
            @Override
            public TableWriter get() {
                Preconditions.checkState(output != null, "evaluation not running");
                return output;
            }
        };
    }

    /**
     * Get the prediction output table.
     * @return The table writer for the prediction output.
     */
    @Nonnull
    Supplier<TableWriter> predictTableSupplier() {
        return new Supplier<TableWriter>() {
            @Override public TableWriter get() {
                return predictOutput;
            }
        };
    }

    /**
     * Get the user output table.
     * @return The table writer for the prediction output.
     */
    @Nonnull
    Supplier<TableWriter> userTableSupplier() {
        return new Supplier<TableWriter>() {
            @Override public TableWriter get() {
                return userOutput;
            }
        };
    }

    @Nonnull
    public List<JobGroup> getJobGroups() {
        return jobGroups;
    }

    /**
     * Function version of {@link #prefixTable(TableWriter, AlgorithmInstance, TTDataSet)}. Intended
     * for use with {@link com.google.common.base.Suppliers#compose(com.google.common.base.Function,Supplier)}.
     */
    public Function<TableWriter,TableWriter> prefixFunction(
            final AlgorithmInstance algorithm,
            final TTDataSet dataSet) {
        return new Function<TableWriter,TableWriter>() {
            @Override
            public TableWriter apply(TableWriter base) {
                return prefixTable(base, algorithm, dataSet);
            }
        };
    }

    /**
     * Prefix a table for a particular algorithm and data set.
     * @param base The table to prefix.
     * @param algorithm The algorithm to prefix for.
     * @param dataSet The data set to prefix for.
     * @return A prefixed table, suitable for outputting the results of evaluating
     * {@code algorithm} on {@code dataSet}, or {@code null} if {@code base} is null.
     */
    public TableWriter prefixTable(TableWriter base,
                                   AlgorithmInstance algorithm, TTDataSet dataSet) {
        if (base == null) return null;

        String[] prefix = new String[commonColumnCount];
        prefix[0] = algorithm.getName();
        for (Map.Entry<String,Object> attr: dataSet.getAttributes().entrySet()) {
            int idx = dataColumns.get(attr.getKey());
            prefix[idx] = attr.getValue().toString();
        }
        for (Map.Entry<String,Object> attr: algorithm.getAttributes().entrySet()) {
            int idx = algoColumns.get(attr.getKey());
            prefix[idx] = attr.getValue().toString();
        }
        return TableWriters.prefixed(base, prefix);
    }
}
