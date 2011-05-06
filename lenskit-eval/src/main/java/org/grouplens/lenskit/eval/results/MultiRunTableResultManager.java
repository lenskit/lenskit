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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.TaskTimer;
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
    
    private int colRun;
    private int colAlgorithm;
    private int colBuildTime;
    private int colTestTime;
    
    private List<PredictionEvaluator> evaluators;
    private TableWriter writer;
    private TableWriter predictionWriter;

    public MultiRunTableResultManager(List<AlgorithmInstance> algos,
            List<PredictionEvaluator> evals,
            File outfile) {
        evaluators = evals;
        
        TableWriterBuilder twb = new CSVWriterBuilder();
        colRun = twb.addColumn("Run");
        colAlgorithm = twb.addColumn("Algorithm");
        colBuildTime = twb.addColumn("BuildTime");
        colTestTime = twb.addColumn("TestTime");
        for (PredictionEvaluator ev: evaluators) {
            ev.setup(twb);
        }
        
        Writer fw;
        try {
            fw = new FileWriter(outfile);
            writer = twb.makeWriter(fw);
        } catch (IOException e) {
            throw new RuntimeException("Error creating table writer", e);
        }
    }
    
    public void setPredictionOutput(@Nullable File f) throws IOException {
    	if (predictionWriter != null)
    		predictionWriter.finish();
    	predictionWriter = null;
    	
    	if (f != null) {
    		TableWriterBuilder twb = new CSVWriterBuilder();
    		twb.addColumn("Run");
    		twb.addColumn("Algorithm");
    		twb.addColumn("User");
    		twb.addColumn("Item");
    		twb.addColumn("Rating");
    		twb.addColumn("Prediction");
    		predictionWriter = twb.makeWriter(new FileWriter(f));
    	}
    }
    
    public ResultAccumulator makeAccumulator(final String run) {
        return new ResultAccumulator() {
            @Override
            public AlgorithmTestAccumulator makeAlgorithmAccumulator(
                    AlgorithmInstance algo) {
                return new MRTAlgorithmTestAccumulator(run, algo);
            }
        };
    }
    
    public void finish() {
        try {
            writer.finish();
            if (predictionWriter != null)
                predictionWriter.finish();
        } catch (IOException e) {
            throw new RuntimeException("Error closing table writer", e);
        }
    }
    
    class MRTAlgorithmTestAccumulator implements AlgorithmTestAccumulator {
        private List<PredictionEvaluator.Accumulator> evalAccums;
        TaskTimer buildTimer;
        TaskTimer testTimer;
        String run;
        AlgorithmInstance algo;
        
        MRTAlgorithmTestAccumulator(String r, AlgorithmInstance a) {
        	run = r;
        	algo = a;
            evalAccums = new ArrayList<PredictionEvaluator.Accumulator>(evaluators.size());
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
            logger.info("Build of {} finished in {}", algo.getName(),
                    buildTimer.elapsedPretty());
        }

        @Override
        public void startTestTimer() {
            testTimer = new TaskTimer();
        }

        @Override
        public void finish() {
            testTimer.stop();
            logger.info("Test of {} finished in {}", algo.getName(), testTimer.elapsedPretty());
            
            writer.startRow();
            try {
                writer.setValue(colRun, run);
                writer.setValue(colAlgorithm, algo.getName());
                writer.setValue(colBuildTime, buildTimer.elapsed());
                writer.setValue(colTestTime, testTimer.elapsed());

                for (PredictionEvaluator.Accumulator ea: evalAccums) {
                    ea.finalize(writer);
                }
            } catch (RuntimeException e) {
                writer.cancelRow();
                throw e;
            }
            
            try {
                writer.finishRow();
            } catch (IOException e) {
                logger.error("Error finishing row: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public void evaluatePrediction(long user, SparseVector ratings,
                SparseVector predictions) {
        	if (predictionWriter != null) {
        		try {
        			for (Long2DoubleMap.Entry r: ratings) {
        				long iid = r.getLongKey();
        				double p = predictions.get(iid);
        				predictionWriter.writeRow(run, algo.getName(), user, iid,
        						r.getDoubleValue(),
        						Double.isNaN(p) ? "NA" : p);
        			}
        		} catch (IOException e) {
        			logger.error("Error writing prediction", e);
        			predictionWriter = null;
        		}
        	}
            for (PredictionEvaluator.Accumulator ea: evalAccums) {
                ea.evaluatePredictions(user, ratings, predictions);
            }
        }
    }
}
