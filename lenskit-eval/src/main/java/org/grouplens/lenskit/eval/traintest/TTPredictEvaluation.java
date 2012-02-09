/*
 * LensKit, an open source recommender systems toolkit.
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

import static com.google.common.collect.Iterables.concat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.io.Closeables;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.Evaluation;
import org.grouplens.lenskit.eval.JobGroup;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.predict.PredictEvalMetric;
import org.grouplens.lenskit.util.tablewriter.CSVWriterBuilder;
import org.grouplens.lenskit.util.tablewriter.TableWriter;
import org.grouplens.lenskit.util.tablewriter.TableWriterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Evaluate several algorithms' prediction accuracy in a train-test
 * configuration over multiple data sets.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public class TTPredictEvaluation implements Evaluation {
    private static final Logger logger = LoggerFactory.getLogger(TTPredictEvaluation.class);

    private File outputFile;
    private File predictOutFile;
    private List<AlgorithmInstance> algorithms;
    private List<PredictEvalMetric> metrics;
    private List<TTDataSet> dataSources;
    private TableWriterBuilder outputBuilder;
    private TableWriterBuilder predictBuilder;
    
    private TableWriter output;
    private TableWriter predictOutput;

    private List<JobGroup> jobGroups;

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outf) {
        outputFile = outf;
    }

    public File getPredictionOutputFile() {
        return predictOutFile;
    }

    /**
     * Set a file to which to write individual predictions produced by the algorithms.
     * @param outf The file to write predictions to, or {@code null} to disable prediction
     *             output.
     */
    public void setPredictionOutputFile(File outf) {
        predictOutFile = outf;
    }

    public List<AlgorithmInstance> getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(List<AlgorithmInstance> algorithms) {
        this.algorithms = algorithms;
    }

    public List<PredictEvalMetric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<PredictEvalMetric> metrics) {
        this.metrics = metrics;
    }

    public List<TTDataSet> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<TTDataSet> sources) {
        dataSources = sources;
    }

    protected void setupJobs() {
        Preconditions.checkState(dataSources != null, "data sources not configured");
        Preconditions.checkState(algorithms != null, "algorithms not configured");
        Preconditions.checkState(metrics != null, "metrics not configured");

        Map<String,Integer> dsColumns =
                indexColumns(1,
                             Lists.transform(dataSources,
                                             new Function<TTDataSet, Map<String, ?>>() {
                                                 @Override
                                                 public Map<String, ?> apply(TTDataSet ds) {
                                                     return ds.getAttributes();
                                                 }
                                             }));
        Map<String,Integer> aiColumns =
                indexColumns(1 + dsColumns.size(),
                             Lists.transform(algorithms,
                                             new Function<AlgorithmInstance, Map<String,?>>() {
                                 @Override
                                 public Map<String,?> apply(AlgorithmInstance ai) {
                                     return ai.getAttributes();
                                 }
                             }));

        jobGroups = new ArrayList<JobGroup>(dataSources.size());
        for (TTDataSet ds: dataSources) {
            TTPredictEvalJobGroup group;
            group = new TTPredictEvalJobGroup(this, algorithms, metrics, dsColumns, aiColumns, ds);
            jobGroups.add(group);
        }

        // Set up the columns & output builder
        int evalColCount = 0;
        for (PredictEvalMetric ev: metrics) {
            evalColCount += ev.getColumnLabels().length;
        }
        final int dacc = dsColumns.size() + aiColumns.size();
        String[] headers = new String[3 + dacc + evalColCount];
        String[] predHeaders = new String[1 + dacc + 4];
        predHeaders[0] = headers[0] = "Algorithm";
        for (Map.Entry<String,Integer> col: concat(dsColumns.entrySet(), aiColumns.entrySet())) {
            headers[col.getValue()] = col.getKey();
            predHeaders[col.getValue()] = col.getKey();
        }
        int index = dacc + 1;
        headers[index++] = "BuildTime";
        headers[index++] = "TestTime";
        for (PredictEvalMetric ev: metrics) {
            for (String c: ev.getColumnLabels()) {
                headers[index++] = c;
            }
        }
        predHeaders[dacc + 1] = "User";
        predHeaders[dacc + 2] = "Item";
        predHeaders[dacc + 3] = "Rating";
        predHeaders[dacc + 4] = "Prediction";

        outputBuilder = new CSVWriterBuilder(outputFile);
        outputBuilder.setColumns(headers);
        if (predictOutFile != null) {
            predictBuilder = new CSVWriterBuilder(predictOutFile);
            predictBuilder.setColumns(predHeaders);
        }
    }

    static Map<String,Integer> indexColumns(int startIndex, Iterable<Map<String,?>> maps) {
        int idx = startIndex;
        Map<String,Integer> columns = new LinkedHashMap<String, Integer>();
        for (Map<String,?> map: maps) {
            for (String k: map.keySet()) {
                if (!columns.containsKey(k)) {
                    columns.put(k, idx++);
                }
            }
        }
        assert startIndex + columns.size() == idx;
        return columns;
    }

    @Override
    public void start() {
        Preconditions.checkState(outputFile != null, "output file not configured");
        setupJobs();

        logger.info("Starting evaluation");
        try {
            output = outputBuilder.open();
        } catch (IOException e) {
            throw new RuntimeException("Error opening output " + outputFile, e);
        }
        if (predictOutput != null) {
            try {
                predictOutput = predictBuilder.open();
            } catch (IOException e) {
                Closeables.closeQuietly(output);
                throw new RuntimeException("Error opening output " + predictOutFile, e);
            }
        }
    }

    @Override
    public void finish() {
        logger.info("Evaluation finished");
        try {
            output.close();
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
                if (output == null) {
                    throw new IllegalStateException("Evaluation not running");
                } else {
                    return output;
                }
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

    @Override @Nonnull
    public List<JobGroup> getJobGroups() {
        if (jobGroups == null) {
            throw new IllegalStateException("evaluation not started");
        }
        return jobGroups;
    }
}
