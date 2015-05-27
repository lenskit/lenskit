/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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

import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.data.source.DataSource;
import org.grouplens.lenskit.data.source.GenericDataSource;
import org.grouplens.lenskit.eval.EvalConfig;
import org.grouplens.lenskit.eval.EvalProject;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstanceBuilder;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.Metric;
import org.grouplens.lenskit.util.table.Table;
import org.lenskit.eval.crossfold.CrossfoldMethods;
import org.lenskit.eval.crossfold.Crossfolder;
import org.lenskit.eval.crossfold.FractionPartition;
import org.lenskit.eval.crossfold.RandomOrder;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * Simplified Java API to train-test evaluation. The train-test evaluator is somewhat difficult to use directly from
 * Java; this class is intended to make it easier.
 */
@SuppressWarnings("unused")
public class SimpleEvaluator implements Callable<Table> {
    private final EvalProject project;
    private List<Crossfolder> crossfolders;
    private TrainTestEvalTask result;
    private Path workDir;

    /**
     * Construct a simple evaluator.
     */
    public SimpleEvaluator() {
        this(null);
    }

    /**
     * Create a simple evaluator with a custom configuration.
     *
     * @param props Properties for the eval configuration.
     */
    public SimpleEvaluator(Properties props) {
        project = new EvalProject(props, null);
        result = new TrainTestEvalTask("simple-eval");
        result.setProject(project);
        result.setOutput((File) null);
        crossfolders = new ArrayList<>();
    }

    /**
     * Get the working directory for the evaluator.
     * @return The directory in which the evaluator will save its working files.
     */
    public Path getWorkDir() {
        return workDir;
    }

    /**
     * Set the working directory for the evaluator.
     * @param dir The directory in which the evaluator will save its output and temporary files.
     * @return The evaluator (for chaining).
     */
    public SimpleEvaluator setWorkDir(Path dir) {
        workDir = dir;
        return this;
    }

    public EvalConfig getEvalConfig() {
        return project.getConfig();
    }

    /**
     * Adds an algorithmInfo to the {@code TrainTestEvalCommand} being built.
     *
     * If any exception is thrown while the command is called it is rethrown as a runtime error.
     * @param algo The algorithmInfo added to the {@code TrainTestEvalCommand}
     * @return Itself to allow  chaining
     */
    public SimpleEvaluator addAlgorithm(AlgorithmInstance algo){
        result.addAlgorithm(algo);
        return this;
    }

    /**
     * An algorithm instance constructed with a name and Lenskit configuration
     * @param name The name of the algorithm.
     * @param config Lenskit configuration
     *
     */
    public SimpleEvaluator addAlgorithm(String name, LenskitConfiguration config) {
        result.addAlgorithm(new AlgorithmInstance(name, config));
        return this;
    }

    /**
     * Adds a fully configured algorithmInfo command to the {@code TrainTestEvalCommand} being built.
     *
     * @param algo The algorithmInfo added to the {@code TrainTestEvalCommand}
     * @return Itself to allow  chaining
     */
    public SimpleEvaluator addAlgorithm(AlgorithmInstanceBuilder algo){
        result.addAlgorithm(algo.build());
        return this;
    }


    /**
     * Calls the {@code CrossfoldCommand} and adds the resulting {@code TTDataSet}s to the {@code TrainTestEvalCommand}.
     *
     * Any exceptions that are thrown are wrapped as {@code RuntimeExceptions}.
     *
     * @param cross The crossfold task.
     * @return Itself to allow for  method chaining.
     */
    public SimpleEvaluator addDataset(Crossfolder cross){
        crossfolders.add(cross);
        for (TTDataSet data: cross.getDataSets()) {
            result.addDataset(data);
        }
        return this;
    }

    /**
     * Add a new data set to be cross-folded.  This method creates a new {@link Crossfolder}
     * and passes it to {@link #addDataset(Crossfolder)}.  All crossfold parameters that are not
     * taken as arguments by this method are left at their defaults.
     *
     * @param name The name of the crossfold
     * @param source The source for the crossfold
     * @param partitions The number of partitions
     * @param holdout The holdout fraction
     * @return Itself for chaining.
     */
    public SimpleEvaluator addDataset(String name, DataSource source, int partitions, double holdout){
        Crossfolder cross = new Crossfolder(name)
                .setSource(source)
                .setPartitionCount(partitions)
                .setMethod(CrossfoldMethods.partitionUsers(new RandomOrder<Rating>(),
                                                           new FractionPartition<Rating>(holdout)))
                .setOutputDir(workDir.resolve(name + ".split"));
        addDataset(cross);
        return this;
    }

    /**
     * Add a new data set to be cross-folded.  This method creates a new {@link Crossfolder}
     * and passes it to {@link #addDataset(Crossfolder)}.  All crossfold parameters that are not
     * taken as arguments by this method are left at their defaults.
     *
     * @param source The source for the crossfold
     * @param partitions The number of partitions
     * @param holdout The holdout fraction
     * @return Itself for chaining.
     */
    public SimpleEvaluator addDataset(DataSource source, int partitions, double holdout){
        return addDataset(source.getName(), source, partitions, holdout);
    }
    /**
     * Add a new data set to be cross-folded.  This method creates a new {@link Crossfolder}
     * and passes it to {@link #addDataset(Crossfolder)}.  All crossfold parameters that are not
     * taken as arguments by this method are left at their defaults.
     * <p>
     * <strong>Note:</strong> Prior to LensKit 2.2, this method used a holdout fraction of 0.2. In
     * LensKit 2.2, it was changed to use the {@link Crossfolder}'s default holdout.
     * </p>
     *
     * @param name The name of the crossfold
     * @param source The source for the crossfold
     * @param partitions The number of partitions
     * @return Itself for chaining.
     */
    public SimpleEvaluator addDataset(String name, DataSource source, int partitions){
        return addDataset(new Crossfolder(name).setSource(source)
                                               .setPartitionCount(partitions)
                                               .setOutputDir(workDir.resolve(name + ".split")));
    }

    /**
     * Add a new data set to be cross-folded.  This method creates a new {@link Crossfolder}
     * and passes it to {@link #addDataset(Crossfolder)}.  All crossfold parameters that are not
     * taken as arguments by this method are left at their defaults.
     * <p>
     * <strong>Note:</strong> Prior to LensKit 2.2, this method used a holdout fraction of 0.2. In
     * LensKit 2.2, it was changed to use the {@link Crossfolder}'s default holdout.
     * </p>
     *
     * @param source The source for the crossfold
     * @param partitions The number of partitions
     * @return Itself for chaining.
     */
    public SimpleEvaluator addDataset(DataSource source, int partitions){
        return addDataset(source.getName(), source, partitions);
    }

    /**
     * Adds a single {@code TTDataSet} to the {@code TrainTestEvalCommand}.
     *
     * This acts a wrapper around {@code TrainTestEvalCommand.addDataset}
     * @param data The dataset to be added to the command.
     * @return Itself to allow for  method chaining.
     */
    public SimpleEvaluator addDataset(TTDataSet data) {
        result.addDataset(data);
        return this;
    }

    /**
     * This constructs a new {@code TTDataSet} and passes it to the {@code TrainTestEvalCommand}.
     * @param name The name of the new dataset.
     * @param train The {@code DAOFactory} with the train data.
     * @param test The {@code DAOFactory} with the test data.
     * @param dom The {@code PreferenceDomain} to be supplied to the new {@code TTDataSet}
     * @return Itself for  method chaining.
     */
    public SimpleEvaluator addDataset(String name, EventDAO train, EventDAO test, PreferenceDomain dom){
        result.addDataset(GenericTTDataSet.newBuilder(name)
                                          .setTrain(new GenericDataSource(name + ".train", train, dom))
                                          .setTest(new GenericDataSource(name + ".test", test, dom))
                                          .build());
        return this;
    }

    /**
     * This constructs a new {@code TTDataSet} and passes it to the {@code TrainTestEvalCommand}.
     *
     * The name for the data source will default to 'generic-data-source'. Because of this,
     * be careful of calling this method more than once.
     *
     * @param train The {@code DAOFactory} with the train data.
     * @param test The {@code DAOFactory} with the test data.
     * @return Itself for  method chaining.
     */
    public SimpleEvaluator addDataset(DataSource train, DataSource test){
        result.addDataset(GenericTTDataSet.newBuilder("generic-data-source")
                                          .setTrain(train)
                                          .setTest(test)
                                          .build());
        return this;
    }

    /**
     * Adds a completed metric to the {@code TrainTestEvalCommand}
     * @param metric The metric to be added.
     * @return Itself for  method chaining.
     */
    public SimpleEvaluator addMetric(Metric<?> metric) {
        result.addMetric(metric);
        return this;
    }

    /**
     * Adds a completed metric to the {@code TrainTestEvalCommand}
     * @param metric The metric to be added.
     * @return Itself for  method chaining.
     */
    public <T> SimpleEvaluator addMetric(Class<? extends Metric<T>> metric) {
        result.addMetric(metric);
        return this;
    }

    /**
     * This provides a wrapper around {@code TrainTestEvalCommand.setOutput()}
     * @param file The file set as the output of the command
     * @return Itself for  method chaining
     */
    public SimpleEvaluator setOutput(File file){
        result.setOutput(file);
        return this;
    }

    /**
     * This provides a wrapper around {@code TrainTestEvalCommand.setPredictOutput}
     * @param file The file set as the prediction output.
     * @return The evaluator (for chaining)
     */
    public SimpleEvaluator setPredictOutput(File file){
        OutputPredictMetric.FactoryBuilder factory = new OutputPredictMetric.FactoryBuilder();
        factory.setFile(file);
        result.addMetric(factory.build());
        return this;
    }


    /**
     * This provides a wrapper around {@code TrainTestEvalCommand.setUserOutput}
     * @param file The file set as the prediction user.
     * @return The evaluator (for chaining)
     */
    public SimpleEvaluator setUserOutput(File file){
        result.setUserOutput(file);
        return this;
    }

    /**
     * Creates a new file with the {@code name} and passes it to
     * {@code TrainTestEvalCommand.setOutput()}
     * @param path The path to the file to be created
     * @return Itself for method chaining
     */
    public SimpleEvaluator setOutputPath(String path){
        result.setOutput(new File(path));
        return this;
    }
    /**
     * Creates a new file with the {@code name} and passes it to
     * {@code TrainTestEvalCommand.setPredictOutput()}
     * @param path The path to the file to be created
     * @return Itself for method chaining
     */
    public SimpleEvaluator setPredictOutputPath(String path){
        return setPredictOutput(new File(path));
    }

    /**
     * Creates a new file with the {@code name} and passes it to
     * {@code TrainTestEvalCommand.setUserOutput()}
     * @param path The path to the file to be created
     * @return Itself for method chaining
     */
    public SimpleEvaluator setUserOutputPath(String path){
        result.setUserOutput(new File(path));
        return this;
    }

    /**
     * Provides raw unrestricted access for the command.
     *
     * Use this with caution! Calling certain methods on the {@code TrainTestDataSet} can force this command to throw
     * an exception farther down the line.
     *
     * @return The raw partially configured command.
     */
    public TrainTestEvalTask getRawCommand(){
        return result;
    }

    /**
     * If this is called more than once it will call of these commands again and most likely throw an exception.
     *
     * @return The table resulting from calling the command.
     */
    @Override
    public Table call() throws TaskExecutionException {
        result.setProject(project);
        try {
            for (Crossfolder cf: crossfolders) {
                cf.run();
            }
            return result.perform();
        } catch (InterruptedException e) {
            throw new TaskExecutionException("execution interrupted", e);
        }
    }
}

