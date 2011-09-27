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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.grouplens.lenskit.dtree.DataNode;
import org.grouplens.lenskit.dtree.Trees;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.Evaluation;
import org.grouplens.lenskit.eval.Evaluator;
import org.grouplens.lenskit.eval.EvaluatorConfigurationException;
import org.grouplens.lenskit.eval.InvalidRecommenderException;
import org.grouplens.lenskit.eval.data.traintest.TTDataProvider;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.predict.PredictionEvaluator;
import org.grouplens.lenskit.util.spi.ConfigAlias;
import org.grouplens.lenskit.util.spi.ServiceFinder;
import org.kohsuke.MetaInfServices;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Train-test evaluator that builds on a training set and runs on a test set.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ConfigAlias("train-test")
@MetaInfServices
public class TrainTestEvaluator implements Evaluator {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestEvaluator.class);
    private static final String[] DEFAULT_EVALUATORS = {
        "coverage", "MAE", "RMSE", "nDCG"
    };

    @Override
    public String getName() {
        return "Train-Test";
    }

    @Override
    public Evaluation configure(Properties properties, DataNode config)
            throws EvaluatorConfigurationException {
        logger.debug("Configuring evaluator from {}", config);
        List<PredictionEvaluator> evals = configureEvaluators(properties, config);
        logger.debug("Have {} evaluators", evals.size());
        List<AlgorithmInstance> algos = configureAlgorithms(properties, config);
        if (algos.isEmpty())
            throw new EvaluatorConfigurationException("No algorithms configured");
        File output = configureOutput(properties, config);
        List<TTDataSet> data = configureDataSources(properties, config);
        if (data.isEmpty())
            throw new EvaluatorConfigurationException("No data sources configured");
        return new TTPredictEvaluation(output, algos, evals, data);
    }

    /**
     * @param properties
     * @param config
     * @return
     * @throws EvaluatorConfigurationException 
     */
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

    /**
     * @param properties
     * @param config
     * @return
     * @throws EvaluatorConfigurationException 
     */
    private File configureOutput(Properties properties, DataNode config) 
            throws EvaluatorConfigurationException {
        String out = Trees.childValue(config, "output");
        if (out == null)
            throw new EvaluatorConfigurationException("No output file specified");
        logger.debug("Output to {}", out);
        return new File(out);
    }

    /**
     * @param properties
     * @param config
     * @return
     * @throws EvaluatorConfigurationException 
     */
    private List<AlgorithmInstance> configureAlgorithms(Properties properties,
                                                        DataNode config) throws EvaluatorConfigurationException {
        DataNode algosElt = Trees.child(config, "algorithms");
        if (algosElt == null)
            return Collections.emptyList();
        
        List<AlgorithmInstance> algos = new ArrayList<AlgorithmInstance>();
        for (DataNode ae: algosElt.getChildren()) {
            if (!ae.getName().equals("script"))
                throw new EvaluatorConfigurationException("Unexpected element " + ae.getName());
            
            // TODO Support glob expressions
            String fn = ae.getValue();
            try {
                algos.addAll(loadAlgorithm(properties, new File(fn)));
            } catch (InvalidRecommenderException e) {
                throw new EvaluatorConfigurationException(e);
            }
        }
        return algos;
    }
    
    private List<AlgorithmInstance> loadAlgorithm(Properties properties, File file) throws InvalidRecommenderException {
        logger.info("Loading recommender definition from {}", file);
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

    /**
     * @param properties
     * @param config
     * @return
     * @throws EvaluatorConfigurationException 
     */
    private List<PredictionEvaluator> configureEvaluators(Properties properties,
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
        ServiceFinder<PredictionEvaluator> finder = ServiceFinder.get(PredictionEvaluator.class);
        List<PredictionEvaluator> evaluators = new ArrayList<PredictionEvaluator>(evalNames.size());
        for (String name: evalNames) {
            PredictionEvaluator eval = finder.findProvider(name);
            if (eval == null) {
                throw new EvaluatorConfigurationException("Unknown evaluator " + name);
            }
            evaluators.add(eval);
        }
        return evaluators;
    }
}
