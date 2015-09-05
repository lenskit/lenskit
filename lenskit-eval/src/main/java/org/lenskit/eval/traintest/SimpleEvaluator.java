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
package org.lenskit.eval.traintest;

import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.source.DataSource;
import org.grouplens.lenskit.data.source.GenericDataSource;
import org.grouplens.lenskit.eval.metrics.Metric;
import org.grouplens.lenskit.util.table.Table;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.Rating;
import org.lenskit.eval.crossfold.Crossfolder;
import org.lenskit.eval.crossfold.FractionPartition;
import org.lenskit.eval.crossfold.RandomOrder;
import org.lenskit.eval.crossfold.SplitMethods;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified Java API to train-test evaluation. The train-test evaluator is somewhat difficult to use directly from
 * Java; this class is intended to make it easier.
 */
@SuppressWarnings("unused")
public class SimpleEvaluator {
    private List<Crossfolder> crossfolders;
    private TrainTestExperiment experiment;
    private Path workDir;

    /**
     * Create a simple evaluator with a custom configuration.
     */
    public SimpleEvaluator() {
        experiment = new TrainTestExperiment();
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

    /**
     * Adds an algorithmInfo to the experiment being built.
     *
     * If any exception is thrown while the command is called it is rethrown as a runtime error.
     * @param algo The algorithm to add to the experiment.
     * @return The evaluator (for chaining).
     */
    public SimpleEvaluator addAlgorithm(AlgorithmInstance algo){
        experiment.addAlgorithm(algo);
        return this;
    }

    /**
     * An algorithm instance constructed with a name and Lenskit configuration
     * @param name The name of the algorithm.
     * @param config Lenskit configuration
     *
     */
    public SimpleEvaluator addAlgorithm(String name, LenskitConfiguration config) {
        experiment.addAlgorithm(new AlgorithmInstance(name, config));
        return this;
    }

    /**
     * Adds a crossfolder's results to the experiment.
     *
     * @param cross The crossfold task.
     * @return The simple evaluator (for chaining).
     */
    public SimpleEvaluator addDataSet(Crossfolder cross){
        crossfolders.add(cross);
        experiment.addDataSets(cross.getDataSets());
        return this;
    }

    /**
     * Add a new data set to be cross-folded.  This method creates a new {@link Crossfolder}
     * and passes it to {@link #addDataSet(Crossfolder)}.  All crossfold parameters that are not
     * taken as arguments by this method are left at their defaults.
     *
     * @param name The name of the crossfold
     * @param source The source for the crossfold
     * @param partitions The number of partitions
     * @param holdout The holdout fraction
     * @return Itself for chaining.
     */
    public SimpleEvaluator addDataSet(String name, DataSource source, int partitions, double holdout){
        Crossfolder cross = new Crossfolder(name)
                .setSource(source)
                .setPartitionCount(partitions)
                .setMethod(SplitMethods.partitionUsers(new RandomOrder<Rating>(),
                                                       new FractionPartition<Rating>(holdout)))
                .setOutputDir(workDir.resolve(name + ".split"));
        addDataSet(cross);
        return this;
    }

    /**
     * Add a new data set to be cross-folded.  This method creates a new {@link Crossfolder}
     * and passes it to {@link #addDataSet(Crossfolder)}.  All crossfold parameters that are not
     * taken as arguments by this method are left at their defaults.
     *
     * @param source The source for the crossfold
     * @param partitions The number of partitions
     * @param holdout The holdout fraction
     * @return Itself for chaining.
     */
    public SimpleEvaluator addDataSet(DataSource source, int partitions, double holdout){
        return addDataSet(source.getName(), source, partitions, holdout);
    }

    /**
     * Add a new data set to be cross-folded.  This method creates a new {@link Crossfolder}
     * and passes it to {@link #addDataSet(Crossfolder)}.  All crossfold parameters that are not
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
    public SimpleEvaluator addDataSet(String name, DataSource source, int partitions){
        return addDataSet(new Crossfolder(name).setSource(source)
                                               .setPartitionCount(partitions)
                                               .setOutputDir(workDir.resolve(name + ".split")));
    }

    /**
     * Add a new data set to be cross-folded.  This method creates a new {@link Crossfolder}
     * and passes it to {@link #addDataSet(Crossfolder)}.  All crossfold parameters that are not
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
    public SimpleEvaluator addDataSet(DataSource source, int partitions){
        return addDataSet(source.getName(), source, partitions);
    }

    /**
     * Adds a single {@link DataSet} to the {@link TrainTestExperiment}.
     *
     * @param data The data set to be added to the command.
     * @return The simple evaluator (for chaining)
     */
    public SimpleEvaluator addDataSet(DataSet data) {
        experiment.addDataSet(data);
        return this;
    }

    /**
     * Add a data set to the experiment by DAO.
     * @param name The name of the new dataset.
     * @param train The DAO containing training data.
     * @param test The DAO containing test data.
     * @param dom The {@link PreferenceDomain} for the ratings.
     * @return The evaluator (for chaining).
     */
    public SimpleEvaluator addDataSet(String name, EventDAO train, EventDAO test, PreferenceDomain dom){
        experiment.addDataSet(DataSet.newBuilder(name)
                                     .setTrain(new GenericDataSource(name + ".train", train, dom))
                                     .setTest(new GenericDataSource(name + ".test", test, dom))
                                     .build());
        return this;
    }

    /**
     * Add a data set to the experiment.
     *
     * The name for the data source will default to 'generic-data-source'. Because of this,
     * be careful of calling this method more than once.
     *
     * @param train The source of training data.
     * @param test The source of test data.
     * @return The evaluator (for chaining).
     */
    public SimpleEvaluator addDataSet(DataSource train, DataSource test){
        experiment.addDataSet(DataSet.newBuilder("generic-data-source")
                                     .setTrain(train)
                                     .setTest(test)
                                     .build());
        return this;
    }

    /**
     * Add a metric to the experiment.
     *
     * @param metric The metric to be added.
     * @return The evaluator (for chaining).
     */
    public SimpleEvaluator addMetric(Metric<?> metric) {
        // TODO add metrics
        return this;
    }

    /**
     * Add a metric to the experiment.
     *
     * @param metric The metric to be added.
     * @return The evaluator (for chaining).
     */
    public <T> SimpleEvaluator addMetric(Class<? extends Metric<T>> metric) {
        // TODO add metrics
        return this;
    }

    /**
     * Set an output file for aggregate metrics.
     * @param file An output file for aggregate metrics.
     * @return The evaluator (for chaining).
     */
    public SimpleEvaluator setOutput(Path file){
        experiment.setOutputFile(file);
        return this;
    }

    /**
     * Set an output file for per-user evaluation metrics.
     * @param file A file to receive per-user evaluation metrics.
     * @return The evaluator (for chaining).
     */
    public SimpleEvaluator setUserOutput(Path file){
        experiment.setUserOutputFile(file);
        return this;
    }

    /**
     * Provides raw unrestricted access to the experiment.
     *
     * @return The raw partially configured experiment.
     */
    public TrainTestExperiment getExperiment(){
        return experiment;
    }

    /**
     * If this is called more than once it will call of these commands again and most likely throw an exception.
     *
     * @return The table resulting from calling the command.
     */
    public Table execute() {
        for (Crossfolder cf: crossfolders) {
            cf.execute();
        }
        return experiment.run();
    }
}

