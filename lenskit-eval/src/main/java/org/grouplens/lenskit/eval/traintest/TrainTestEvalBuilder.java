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

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.predict.PredictEvalMetric;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Train-test evaluator that builds on a training set and runs on a test set.
 * 
 * @since 0.10
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TrainTestEvalBuilder implements Builder<TTPredictEvaluation> {
    private List<TTDataSet> dataSources;
    private List<AlgorithmInstance> algorithms;
    private List<PredictEvalMetric> metrics;
    private File outputFile;
    private File userOutputFile;
    private File predictOutputFile;

    public TrainTestEvalBuilder() {
        dataSources = new LinkedList<TTDataSet>();
        algorithms = new LinkedList<AlgorithmInstance>();
        metrics = new LinkedList<PredictEvalMetric>();
        outputFile = new File("train-test-results.csv");
    }

    @Override
    public TTPredictEvaluation build() {
        return new TTPredictEvaluation(dataSources, algorithms, metrics,
                                       outputFile, userOutputFile, predictOutputFile);
    }

    public void addDataset(TTDataSet source) {
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

    public void setUserOutput(File file) {
        userOutputFile = file;
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
}
