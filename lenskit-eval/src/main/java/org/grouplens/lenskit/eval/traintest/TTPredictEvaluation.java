/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.eval.*;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.EvalMetric;
import org.grouplens.lenskit.util.tablewriter.CSVWriter;
import org.grouplens.lenskit.util.tablewriter.TableLayout;
import org.grouplens.lenskit.util.tablewriter.TableLayoutBuilder;
import org.grouplens.lenskit.util.tablewriter.TableWriter;
import org.grouplens.lenskit.util.tablewriter.TableWriters;
import org.picocontainer.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Closeables;

/**
 * Evaluate several algorithms' prediction accuracy in a train-test
 * configuration over multiple data sets.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public class TTPredictEvaluation extends AbstractEvalTask  {
    private static final Logger logger = LoggerFactory.getLogger(TTPredictEvaluation.class);

    private final File outputFile;
    private final File userOutputFile;
    private final File predictOutputFile;
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
    private List<EvalMetric> predictMetrics;

    private EvalTaskHelper taskHelper;

    public TTPredictEvaluation(String name, Set<EvalTask> dependency,
                               @Nonnull List<TTDataSet> sources,
                               @Nonnull List<AlgorithmInstance> algos,
                               @Nonnull List<EvalMetric> metrics,
                               @Nonnull File output,
                               @Nullable File userOutput,
                               @Nullable File predictOutput,
                               EvalTaskHelper helper) {
        super(name, dependency);
        outputFile = output;
        userOutputFile = userOutput;
        predictOutputFile = predictOutput;
        taskHelper = helper;

        setupJobs(sources, algos, metrics);
    }

    protected void setupJobs(List<TTDataSet> dataSources,
                             List<AlgorithmInstance> algorithms,
                             List<EvalMetric> metrics) {
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
            TTPredictEvalJobGroup group;
            group = new TTPredictEvalJobGroup(this, algorithms, metrics, dataset);
            jobGroups.add(group);
        }

        commonColumnCount = master.getColumnCount();

        TableLayoutBuilder output = master.clone();
        output.addColumn("BuildTime");
        output.addColumn("TestTime");
        TableLayoutBuilder userOutput = master.clone();

        for (EvalMetric ev: metrics) {
            for (String c: ev.getColumnLabels()) {
                output.addColumn(c);
            }
            for (String c: ev.getUserColumnLabels()) {
                userOutput.addColumn(c);
            }
        }

        outputLayout = output.build();
        userLayout = userOutput.build();

        TableLayoutBuilder predOutput = master.clone();
        predOutput.addColumn("User");
        predOutput.addColumn("Item");
        predOutput.addColumn("Rating");
        predOutput.addColumn("Prediction");

        predictLayout = predOutput.build();

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
        for (EvalMetric metric: predictMetrics) {
            metric.startEvaluation(this);
        }
    }

    public void finish() {
        for (EvalMetric metric: predictMetrics) {
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
     * Run the evaluation.  This method assumes that the evaluation is already
     * prepared — call to do that.
     */
    @Override
    public Void call() throws ExecutionException {
        this.start();

        for (JobGroup group: this.getJobGroups()) {
            taskHelper.getExecutor().add(group);
        }
        try {
            taskHelper.getExecutor().run();
        } finally {
            logger.info("Finishing evaluation");
            this.finish();
        }
        return null;
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
     * for use with {@link Suppliers#compose(Function,Supplier)}.
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
