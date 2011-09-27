/*
 * LensKit, a reference implementation of recommender algorithms.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.Evaluation;
import org.grouplens.lenskit.eval.JobGroup;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.predict.PredictionEvaluator;
import org.grouplens.lenskit.tablewriter.CSVWriterBuilder;
import org.grouplens.lenskit.tablewriter.TableWriter;
import org.grouplens.lenskit.tablewriter.TableWriterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                               List<PredictionEvaluator> evals,
                               List<TTDataSet> dataSources) {
        outputFile = output;
        
        // Collect attribute columns
        Set<String> acols = new LinkedHashSet<String>();
        for (AlgorithmInstance algo: algos) {
            acols.addAll(algo.getAttributes().keySet());
        }
        
        // Collect column indexes
        Map<String,Integer> acIndexes = new HashMap<String, Integer>();
        int idx = 2;
        for (String col: acols) {
            if (!acIndexes.containsKey(col))
                acIndexes.put(col, idx++);
        }
        
        jobGroups = new ArrayList<JobGroup>(dataSources.size());
        for (TTDataSet ds: dataSources) {
            TTPredictEvalJobGroup group;
            group = new TTPredictEvalJobGroup(this, algos, evals, acIndexes, ds);
            jobGroups.add(group);
        }
        
        // Set up the columns & output builder
        outputBuilder = new CSVWriterBuilder();
        List<String> headers = new ArrayList<String>();
        headers.add("Run");
        headers.add("Algorithm");
        for (String c: acols) {
            assert acIndexes.get(c).equals(headers.size());
            headers.add(c);
        }
        headers.add("BuildTime");
        headers.add("TestTime");
        for (PredictionEvaluator ev: evals) {
            for (String c: ev.getColumnLabels()) {
                headers.add(c);
            }
        }
        outputBuilder.setColumns(headers.toArray(new String[headers.size()]));
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
    @Nonnull TableWriter getOutputTable() {
        if (output == null)
            throw new IllegalStateException("Evaluation not running");
        return output;
    }

    @Override
    public List<JobGroup> getJobGroups() {
        return jobGroups;
    }

}
