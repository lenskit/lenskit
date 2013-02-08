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
import com.google.common.base.Supplier;
import com.google.common.io.Closeables;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.eval.*;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.algorithm.ExternalAlgorithmInstance;
import org.grouplens.lenskit.eval.algorithm.LenskitAlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.TestUserMetric;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.util.table.Table;
import org.grouplens.lenskit.util.tablewriter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * The command that run the algorithm instance and output the prediction result file and the evaluation result file
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public class TrainTestEvalCommand extends AbstractCommand<Table> {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestEvalCommand.class);

    private List<TTDataSet> dataSources;
    private List<AlgorithmInstance> algorithms;
    private List<TestUserMetric> metrics;
    private List<Pair<Symbol,String>> predictChannels;
    private IsolationLevel isolationLevel;
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
    private InMemoryWriter outputInMemory;
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
        predictChannels = new LinkedList<Pair<Symbol, String>>();
        outputFile = new File("train-test-results.csv");
        isolationLevel = IsolationLevel.NONE;
    }

    public TrainTestEvalCommand addDataset(TTDataSet source) {
        dataSources.add(source);
        return this;
    }

    public TrainTestEvalCommand addAlgorithm(LenskitAlgorithmInstance algorithm) {
        algorithms.add(algorithm);
        return this;
    }

    public TrainTestEvalCommand addExternalAlgorithm(ExternalAlgorithmInstance algorithm) {
        algorithms.add(algorithm);
        return this;
    }

    public TrainTestEvalCommand addMetric(TestUserMetric metric) {
        metrics.add(metric);
        return this;
    }

    /**
     * Add a channel to be recorded with predict output.
     *
     * @param channelSym The channel to output.
     * @return The command (for chaining)
     * @see #addWritePredictionChannel
     */
    public TrainTestEvalCommand addWritePredictionChannel(@Nonnull Symbol channelSym) {
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
    public TrainTestEvalCommand addWritePredictionChannel(@Nonnull Symbol channelSym,
                                                          @Nullable String label) {
        Preconditions.checkNotNull(channelSym, "channel is null");
        if (label == null) {
            label = channelSym.getName();
        }
        Pair<Symbol, String> entry = Pair.of(channelSym, label);
        predictChannels.add(entry);
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

    List<TTDataSet> dataSources() {
        return dataSources;
    }

    List<AlgorithmInstance> getAlgorithms() {
        return algorithms;
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
     * @return The summary output table.
     * @throws org.grouplens.lenskit.eval.CommandException
     *          Failure of the evaluation
     */
    @Override
    public Table call() throws CommandException {
        this.setupJobs();
        int nthreads = getConfig().getThreadCount();
        logger.info("Starting evaluation");
        this.initialize();
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

        for (JobGroup group : this.getJobGroups()) {
            exec.add(group);
        }
        try {
            exec.run();
        } catch (ExecutionException e) {
            throw new CommandException("Error running the evaluation", e);
        } finally {
            logger.info("Finishing evaluation");
            this.cleanUp();
        }
        return outputInMemory.getResult();
    }

    protected void setupJobs() throws CommandException {
        TableLayoutBuilder master = new TableLayoutBuilder();

        master.addColumn("Algorithm");
        dataColumns = new HashMap<String, Integer>();
        for (TTDataSet ds : dataSources) {
            for (String attr : ds.getAttributes().keySet()) {
                if (!dataColumns.containsKey(attr)) {
                    dataColumns.put(attr, master.addColumn(attr));
                }
            }
        }

        algoColumns = new HashMap<String, Integer>();
        for (AlgorithmInstance algo : algorithms) {
            for (String attr : algo.getAttributes().keySet()) {
                if (!algoColumns.containsKey(attr)) {
                    algoColumns.put(attr, master.addColumn(attr));
                }
            }
        }

        jobGroups = new ArrayList<JobGroup>(dataSources.size());
        int idx = 0;
        for (TTDataSet dataset : dataSources) {
            TrainTestEvalJobGroup group;
            group = new TrainTestEvalJobGroup(this, algorithms, metrics, dataset, idx, numRecs);
            jobGroups.add(group);
            idx++;
        }

        commonColumnCount = master.getColumnCount();

        TableLayoutBuilder output = master.clone();
        output.addColumn("BuildTime");
        output.addColumn("TestTime");
        TableLayoutBuilder perUser = master.clone();

        String[] columnLabels;
        String[] userColumnLabels;

        for (TestUserMetric ev : metrics) {
            columnLabels = ev.getColumnLabels();
            if (columnLabels != null) {
                for (String c : columnLabels) {
                    output.addColumn(c);
                }
            }

            userColumnLabels = ev.getUserColumnLabels();
            if (userColumnLabels != null) {
                for (String c : userColumnLabels) {
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
        for (Pair<Symbol,String> pair: predictChannels) {
            eachPred.addColumn(pair.getRight());
        }

        predictLayout = eachPred.build();

        predictMetrics = metrics;
    }

    private void initialize() {
        logger.info("Starting evaluation");
        List<TableWriter> tableWriters = new ArrayList<TableWriter>();
        outputInMemory = new InMemoryWriter(outputLayout);
        tableWriters.add(outputInMemory);
        if (outputFile != null) {
            try {
                tableWriters.add(CSVWriter.open(outputFile, outputLayout));
            } catch (IOException e) {
                throw new RuntimeException("Error opening output table", e);
            }
        }
        output = new MultiplexedTableWriter(outputLayout, tableWriters);
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
        for (TestUserMetric metric : predictMetrics) {
            metric.startEvaluation(this);
        }
    }

    private void cleanUp() {
        for (TestUserMetric metric : predictMetrics) {
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
     *                               finished.
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
     *
     * @return The table writer for the prediction output.
     */
    @Nonnull
    Supplier<TableWriter> predictTableSupplier() {
        return new Supplier<TableWriter>() {
            @Override
            public TableWriter get() {
                return predictOutput;
            }
        };
    }

    /**
     * Get the user output table.
     *
     * @return The table writer for the prediction output.
     */
    @Nonnull
    Supplier<TableWriter> userTableSupplier() {
        return new Supplier<TableWriter>() {
            @Override
            public TableWriter get() {
                return userOutput;
            }
        };
    }

    @Nonnull
    public List<JobGroup> getJobGroups() {
        return jobGroups;
    }

    /**
     * Function version of {@link #prefixTable(TableWriter, org.grouplens.lenskit.eval.algorithm.AlgorithmInstance, TTDataSet)}. Intended
     * for use with {@link com.google.common.base.Suppliers#compose(com.google.common.base.Function, Supplier)}.
     */
    public Function<TableWriter, TableWriter> prefixFunction(
            final AlgorithmInstance algorithm,
            final TTDataSet dataSet) {
        return new Function<TableWriter, TableWriter>() {
            @Override
            public TableWriter apply(TableWriter base) {
                return prefixTable(base, algorithm, dataSet);
            }
        };
    }

    /**
     * Prefix a table for a particular algorithm and data set.
     *
     * @param base      The table to prefix.
     * @param algorithm The algorithm to prefix for.
     * @param dataSet   The data set to prefix for.
     * @return A prefixed table, suitable for outputting the results of evaluating
     *         {@code algorithm} on {@code dataSet}, or {@code null} if {@code base} is null.
     */
    public TableWriter prefixTable(TableWriter base,
                                   AlgorithmInstance algorithm, TTDataSet dataSet) {
        if (base == null) {
            return null;
        }

        Object[] prefix = new Object[commonColumnCount];
        prefix[0] = algorithm.getName();
        for (Map.Entry<String, Object> attr : dataSet.getAttributes().entrySet()) {
            int idx = dataColumns.get(attr.getKey());
            prefix[idx] = attr.getValue();
        }
        for (Map.Entry<String, Object> attr : algorithm.getAttributes().entrySet()) {
            int idx = algoColumns.get(attr.getKey());
            prefix[idx] = attr.getValue();
        }
        return TableWriters.prefixed(base, prefix);
    }
}
