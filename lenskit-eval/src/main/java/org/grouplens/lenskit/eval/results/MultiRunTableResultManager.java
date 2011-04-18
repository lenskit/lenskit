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
package org.grouplens.lenskit.eval.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.TaskTimer;
import org.grouplens.lenskit.eval.predict.PredictionEvaluationAccumulator;
import org.grouplens.lenskit.eval.predict.PredictionEvaluator;
import org.grouplens.lenskit.tablewriter.CSVWriterBuilder;
import org.grouplens.lenskit.tablewriter.TableWriter;
import org.grouplens.lenskit.tablewriter.TableWriterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Result manaver that generates accumulators for multiple runs.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class MultiRunTableResultManager {
    private static final Logger logger = LoggerFactory.getLogger(MultiRunTableResultManager.class);
    
    static final int COL_RUN = 0;
    static final int COL_ALGORITHM = 1;
    static final int COL_BUILD_TIME = 2;
    static final int COL_TEST_TIME = 3;
    static final int COL_EVAL_BASE = 4;
    
    private List<PredictionEvaluator> evaluators;
    private TableWriter writer;

    public MultiRunTableResultManager(List<AlgorithmInstance> algos,
            List<PredictionEvaluator> evals,
            File outfile) {
        evaluators = evals;
        
        TableWriterBuilder twb = new CSVWriterBuilder();
        twb.addColumn("Run");
        twb.addColumn("Algorithm");
        twb.addColumn("BuildTime");
        twb.addColumn("TestTime");
        for (PredictionEvaluator ev: evaluators) {
            twb.addColumn(ev.getName());
        }
        
        Writer fw;
        try {
            fw = new FileWriter(outfile);
            writer = twb.makeWriter(fw);
        } catch (IOException e) {
            throw new RuntimeException("Error creating table writer", e);
        }
        
    }
    
    public ResultAccumulator makeAccumulator(final int run) {
        return new ResultAccumulator() {
            @Override
            public AlgorithmTestAccumulator makeAlgorithmAccumulator(
                    AlgorithmInstance algo) {
                writer.setValue(COL_RUN, run);
                writer.setValue(COL_ALGORITHM, algo.getName());
                return new MRTAlgorithmTestAccumulator();
            }
        };
    }
    
    public void finish() {
        try {
            writer.finish();
        } catch (IOException e) {
            throw new RuntimeException("Error closing table writer", e);
        }
    }
    
    class MRTAlgorithmTestAccumulator implements AlgorithmTestAccumulator {
        private List<PredictionEvaluationAccumulator> evalAccums;
        TaskTimer buildTimer;
        TaskTimer testTimer;
        
        MRTAlgorithmTestAccumulator() {
            evalAccums = new ArrayList<PredictionEvaluationAccumulator>(evaluators.size());
            for (PredictionEvaluator eval: evaluators) {
                evalAccums.add(eval.makeAccumulator());
            }
        }
        
        @Override
        public void startBuildTimer() {
            buildTimer = new TaskTimer();
        }

        @Override
        public void finishBuild() {
            buildTimer.stop();
            logger.info("Build finished in {}", buildTimer.elapsedPretty());
        }

        @Override
        public void startTestTimer() {
            testTimer = new TaskTimer();
        }

        @Override
        public void finish() {
            testTimer.stop();
            logger.info("Test finished in {}", testTimer.elapsedPretty());
            
            writer.setValue(COL_BUILD_TIME, buildTimer.elapsed());
            writer.setValue(COL_TEST_TIME, testTimer.elapsed());
            
            int col = COL_EVAL_BASE;
            for (PredictionEvaluationAccumulator ea: evalAccums) {
                writer.setValue(col, ea.finish());
                col++;
            }
            try {
                writer.finishRow();
            } catch (IOException e) {
                logger.error("Error finishing row", e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public void evaluatePrediction(SparseVector ratings,
                SparseVector predictions) {
            for (PredictionEvaluationAccumulator ea: evalAccums) {
                ea.evaluatePrediction(ratings, predictions);
            }
        }
    }
}
