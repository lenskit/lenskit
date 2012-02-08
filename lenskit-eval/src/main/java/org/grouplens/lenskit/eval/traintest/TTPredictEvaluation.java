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
    private TableWriterBuilder outputBuilder;
    private TableWriter output;
    private List<JobGroup> jobGroups;

    public TTPredictEvaluation(File output,
                               List<AlgorithmInstance> algos,
                               List<PredictEvalMetric> evals,
                               List<TTDataSet> dataSources) {
        outputFile = output;
        
        Map<String,Integer> dsColumns =
                indexColumns(1,
                             Lists.transform(dataSources,
                                             new Function<TTDataSet, Map<String,?>>() {
                                 @Override
                                 public Map<String,?> apply(TTDataSet ds) {
                                     return ds.getAttributes();
                                 }
                             }));
        Map<String,Integer> aiColumns =
                indexColumns(1 + dsColumns.size(),
                             Lists.transform(algos,
                                             new Function<AlgorithmInstance, Map<String,?>>() {
                                 @Override
                                 public Map<String,?> apply(AlgorithmInstance ai) {
                                     return ai.getAttributes();
                                 }
                             }));
        
        jobGroups = new ArrayList<JobGroup>(dataSources.size());
        for (TTDataSet ds: dataSources) {
            TTPredictEvalJobGroup group;
            group = new TTPredictEvalJobGroup(this, algos, evals, dsColumns, aiColumns, ds);
            jobGroups.add(group);
        }
        
        // Set up the columns & output builder
        outputBuilder = new CSVWriterBuilder();
        int evalColCount = 0;
        for (PredictEvalMetric ev: evals) {
            evalColCount += ev.getColumnLabels().length;
        }
        final int dacc = dsColumns.size() + aiColumns.size();
        String[] headers = new String[3 + dacc + evalColCount];
        headers[0] = "Algorithm";
        for (Map.Entry<String,Integer> col: concat(dsColumns.entrySet(), aiColumns.entrySet())) {
            headers[col.getValue()] = col.getKey();
        }
        int index = dacc + 1;
        headers[index++] = "BuildTime";
        headers[index++] = "TestTime";
        for (PredictEvalMetric ev: evals) {
            for (String c: ev.getColumnLabels()) {
                headers[index++] = c;
            }
        }
        outputBuilder.setColumns(headers);
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
        logger.info("Starting evaluation");
        try {
            Files.createParentDirs(outputFile);
            output = outputBuilder.makeWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            throw new RuntimeException("Error opening output " + outputFile, e);
        }
    }

    @Override
    public void finish() {
        logger.info("Evaluation finished");
        try {
            output.close();
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
     * @return The table writer for this evaluation.
     * @throws IllegalStateException if the job has not been started or is
     *         finished.
     */
    @Nonnull
    TableWriter getOutputTable() {
        if (output == null)
            throw new IllegalStateException("Evaluation not running");
        return output;
    }

    @Override
    public List<JobGroup> getJobGroups() {
        return jobGroups;
    }

}
