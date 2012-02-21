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

import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.Builder;
import org.codehaus.plexus.util.DirectoryScanner;
import org.grouplens.common.spi.ServiceProvider;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.ConfigUtils;
import org.grouplens.lenskit.eval.EvaluatorConfigurationException;
import org.grouplens.lenskit.eval.InvalidRecommenderException;
import org.grouplens.lenskit.eval.data.traintest.TTDataProvider;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.predict.PredictEvalMetric;
import org.grouplens.lenskit.util.dtree.DataNode;
import org.grouplens.lenskit.util.dtree.Trees;
import org.grouplens.lenskit.util.spi.ServiceFinder;
import org.grouplens.lenskit.util.tablewriter.CSVWriterBuilder;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.*;

/**
 * Train-test evaluator that builds on a training set and runs on a test set.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ServiceProvider
public class TrainTestEvalBuilder implements Builder<TTPredictEvaluation> {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestEvalBuilder.class);
    private static final String[] DEFAULT_EVALUATORS = {
        "coverage", "MAE", "RMSE", "nDCG"
    };

    private List<TTDataSet> dataSources;
    private List<AlgorithmInstance> algorithms;
    private List<PredictEvalMetric> metrics;
    private File outputFile;
    private File predictOutputFile;

    public TrainTestEvalBuilder() {
        dataSources = new LinkedList<TTDataSet>();
        algorithms = new LinkedList<AlgorithmInstance>();
        metrics = new LinkedList<PredictEvalMetric>();
        outputFile = new File("train-test-results.csv");
    }

    @Override
    public TTPredictEvaluation build() {
        CSVWriterBuilder outBuilder = new CSVWriterBuilder(outputFile);
        CSVWriterBuilder predBuilder = null;
        if (predictOutputFile != null) {
            predBuilder = new CSVWriterBuilder(predictOutputFile);
        }
        return new TTPredictEvaluation(dataSources, algorithms, metrics,
                                       outBuilder, predBuilder);
    }

    public void addDataSource(TTDataSet source) {
        dataSources.add(source);
    }

    public void addAlgorithm(AlgorithmInstance algorithm) {
        algorithms.add(algorithm);
    }

    public void addMetric(PredictEvalMetric metric) {
        metrics.add(metric);
    }

    public void setOutput(File file) {
        outputFile = file;
    }

    public void setPredictOutput(File file) {
        predictOutputFile = file;
    }

    List<TTDataSet> dataSources() {
        return dataSources;
    }

    List<AlgorithmInstance> getAlgorithms() {
        return algorithms;
    }

    List<PredictEvalMetric> getMetrics() {
        return metrics;
    }

    File getOutput() {
        return outputFile;
    }

    File getPredictOutput() {
        return predictOutputFile;
    }

    /*public Evaluation configure(Properties properties, DataNode config)
            throws EvaluatorConfigurationException {
        logger.debug("Configuring evaluator from {}", config);

        TTPredictEvaluation eval = new TTPredictEvaluation();

        eval.setMetrics(configureMetrics(properties, config));
        logger.debug("Have {} evaluators", eval.getMetrics().size());

        List<AlgorithmInstance> algos = configureAlgorithms(properties, config);
        if (algos.isEmpty()) {
            throw new EvaluatorConfigurationException("No algorithms configured");
        }
        eval.setAlgorithms(algos);

        DataNode out = Trees.child(config, "output");
        if (out == null) {
            throw new EvaluatorConfigurationException("No output file configured");
        } else {
            eval.setOutputBuilder(ConfigUtils.configureTableOutput(out));
        }
        
        DataNode predOut = Trees.child(config, "predictionOutput");
        if (predOut != null) {
            eval.setPredictOutputBuilder(ConfigUtils.configureTableOutput(predOut));
        }

        List<TTDataSet> data = configureDataSources(properties, config);
        if (data.isEmpty()) {
            throw new EvaluatorConfigurationException("No data sources configured");
        }
        eval.setDataSources(data);

        eval.initialize();
        return eval;
    }*/

    private List<TTDataSet> configureDataSources(Properties properties,
                                                 DataNode config) throws EvaluatorConfigurationException {
        List<TTDataSet> sources = new ArrayList<TTDataSet>();
        DataNode sourcesNode = Trees.child(config, "datasets");
        if (sourcesNode == null)
            return sources;
        
        ServiceFinder<TTDataProvider> finder = ServiceFinder.get(TTDataProvider.class);
        for (DataNode sn: sourcesNode.getChildren()) {
            String name = sn.getName();
            logger.debug("Loading data source {}", name);
            TTDataProvider sp = finder.findProvider(name);
            if (sp == null) {
                throw new EvaluatorConfigurationException("Unknown train-test data provider " + name);
            }
            
            sources.addAll(sp.configure(sn));
        }
        
        return sources;
    }

    private List<AlgorithmInstance> configureAlgorithms(Properties properties,
                                                        DataNode config) throws EvaluatorConfigurationException {
        DataNode algosElt = Trees.child(config, "algorithms");
        if (algosElt == null)
            return Collections.emptyList();
        
        List<AlgorithmInstance> algos = new ArrayList<AlgorithmInstance>();
        for (DataNode ae: algosElt.getChildren()) {
            if (ae.getName().equals("script")) {
                algos.addAll(loadScript(properties, ae));
            } else if (ae.getName().equals("scriptset")) {
                algos.addAll(loadScriptSet(properties, ae));
            } else {
                throw new EvaluatorConfigurationException("Unexpected element " + ae.getName());
            }
        }
        return algos;
    }

    private List<AlgorithmInstance> loadScript(Properties properties, DataNode config) throws EvaluatorConfigurationException {
        String fn = config.getValue();
        try {
            return loadAlgorithm(properties, new File(fn));
        } catch (InvalidRecommenderException e) {
            throw new EvaluatorConfigurationException(e);
        }
    }
    
    private List<AlgorithmInstance> loadScriptSet(Properties properties,
                                                  DataNode config) throws EvaluatorConfigurationException {
        DirectoryScanner ds = ConfigUtils.configureScanner(config);
        ds.scan();
        String[] files = ds.getIncludedFiles();
        File base = ds.getBasedir();

        List<AlgorithmInstance> algos = new ArrayList<AlgorithmInstance>();
        for (String fn: files) {
            try {
                algos.addAll(loadAlgorithm(properties, new File(base, fn)));
            } catch (InvalidRecommenderException e) {
                throw new EvaluatorConfigurationException(e);
            }
        }
        return algos;
    }
    
    private List<AlgorithmInstance> loadAlgorithm(Properties properties, File file) throws InvalidRecommenderException {
        logger.info("Loading recommenders from {}", file);
        URI uri = file.toURI();
        Context cx = Context.enter();
        try {
            Scriptable scope = new ImporterTopLevel(cx);

            ScriptedRecipeBuilder builder = new ScriptedRecipeBuilder(scope);
            Object wbld = Context.javaToJS(builder, scope);
            ScriptableObject.putProperty(scope, "recipe", wbld);
            Logger slog = LoggerFactory.getLogger(file.getPath());
            ScriptableObject.putProperty(scope, "logger", Context.javaToJS(slog, scope));

            ScriptableObject.putProperty(scope, "properties", Context.javaToJS(properties, scope));

            Reader r = new FileReader(file);
            try {
                cx.evaluateReader(scope, r, file.getPath(), 1, null);
                return builder.getAlgorithms();
            } finally {
                r.close();
            }
        } catch (IOException e) {
            throw new InvalidRecommenderException(uri, e);
        } finally {
            Context.exit();
        }
    }

    private List<PredictEvalMetric> configureMetrics(Properties properties,
                                                     DataNode config) throws EvaluatorConfigurationException {
        DataNode evalsElt = Trees.child(config, "evaluators");
        List<String> evalNames;
        if (evalsElt == null) {
            evalNames = Lists.newArrayList(DEFAULT_EVALUATORS);
        } else {
            evalNames = new ArrayList<String>();
            for (DataNode node: evalsElt.getChildren()) {
                evalNames.add(node.getName());
            }
        }
        ServiceFinder<PredictEvalMetric> finder = ServiceFinder.get(PredictEvalMetric.class);
        List<PredictEvalMetric> evaluators = new ArrayList<PredictEvalMetric>(evalNames.size());
        for (String name: evalNames) {
            PredictEvalMetric eval = finder.findProvider(name);
            if (eval == null) {
                throw new EvaluatorConfigurationException("Unknown evaluator " + name);
            }
            evaluators.add(eval);
        }
        return evaluators;
    }
}
