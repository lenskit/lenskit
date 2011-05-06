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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.codehaus.plexus.util.FileUtils;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.InvalidRecommenderException;
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
public class AlgorithmEvaluationRecipe {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmEvaluationRecipe.class);
    
    private int colRun;
    private int colAlgorithm;
    private Map<String,Integer> algoAttrCols;
    private int colBuildTime;
    private int colTestTime;
    
    private List<PredictionEvaluator> evaluators;
    private TableWriter writer;
    private TableWriter predictionWriter;
    
    private List<AlgorithmInstance> algorithms;

    public AlgorithmEvaluationRecipe(List<AlgorithmInstance> algos,
            List<PredictionEvaluator> evals,
            File outfile) {
        evaluators = evals;
        algorithms = algos;
        
        TableWriterBuilder twb = new CSVWriterBuilder();
        colRun = twb.addColumn("Run");
        colAlgorithm = twb.addColumn("Algorithm");
        
        algoAttrCols = new HashMap<String, Integer>();
        for (AlgorithmInstance algo: algos) {
            for (String a: algo.getAttributes().keySet()) {
                if (!algoAttrCols.containsKey(a))
                    algoAttrCols.put(a, twb.addColumn(a));
            }
        }
        
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
    
    public List<AlgorithmInstance> getAlgorithms() {
        return algorithms;
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
                
                Map<String,String> attrs = algo.getAttributes();
                for (Map.Entry<String, Integer> ae: algoAttrCols.entrySet()) {
                    String k = ae.getKey();
                    if (attrs.containsKey(k))
                        writer.setValue(ae.getValue(), attrs.get(k));
                }

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
    
    public static AlgorithmEvaluationRecipe load(File sourceFile, File outputFile) throws InvalidRecommenderException {
        logger.info("Loading recommender definition from {}", sourceFile);
        URI uri = sourceFile.toURI();
        String xtn = FileUtils.extension(sourceFile.getName());
        logger.debug("Loading recommender from {} with extension {}", sourceFile, xtn);
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByExtension(xtn);
        if (engine == null)
            throw new InvalidRecommenderException(uri, "Cannot find engine for extension " + xtn);
        ScriptEngineFactory factory = engine.getFactory();
        logger.debug("Using {} {}", factory.getEngineName(), factory.getEngineVersion());
        ScriptedBuilder builder = new ScriptedBuilder();
        mgr.put("recipe", builder);
        try {
            Reader r = new FileReader(sourceFile);
            engine.put(ScriptEngine.FILENAME, sourceFile.toString());
            try {
                engine.eval(r);
                return builder.build(outputFile);
            } finally {
                r.close();
            }
        } catch (ScriptException e) {
            throw new InvalidRecommenderException(uri, e);
        } catch (IOException e) {
            throw new InvalidRecommenderException(uri, e);
        }
    }
    
    /**
     * Scriptable class for building evaluation recipes.
     * @author Michael Ekstrand <ekstrand@cs.umn.edu>
     *
     */
    static class ScriptedBuilder {
        List<PredictionEvaluator> evaluators = new ArrayList<PredictionEvaluator>();
        List<AlgorithmInstance> algorithms = new ArrayList<AlgorithmInstance>();
        
        /**
         * Add an evaluation.
         * @param eval The evaluator to add.
         */
        public void addEval(PredictionEvaluator eval) {
            evaluators.add(eval);
        }
        
        /**
         * Add an evaluation by class.
         * @param eval A class to instantiate to make an evaluator.
         */
        public void addEval(Class<? extends PredictionEvaluator> eval) {
            try {
                evaluators.add(eval.newInstance());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * Add an evaluation by name.
         * @param name The name of the evaluator. Looks for the class
         * <tt>org.grouplens.lenskit.eval.predict.<i>name</i>Evaluator</tt>.
         * @throws ClassNotFoundException if the evaluator cannot be found.
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public void addEval(String name) throws ClassNotFoundException {
            addEval((Class) Class.forName("org.grouplens.lenskit.eval.predict." + name + "Evaluator"));
        }
        
        /**
         * Create a new algorithm and add it to the algorithm list. The script
         * should then fill in the algorithm's details.
         * @return
         */
        public AlgorithmInstance addAlgorithm() {
            AlgorithmInstance a = new AlgorithmInstance();
            algorithms.add(a);
            return a;
        }
        
        AlgorithmEvaluationRecipe build(File file) {
            return new AlgorithmEvaluationRecipe(algorithms, evaluators, file);
        }
    }
}
