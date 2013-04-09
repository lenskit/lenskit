package org.grouplens.lenskit.eval.traintest;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AbstractCommand;
import org.grouplens.lenskit.eval.CommandException;
import org.grouplens.lenskit.eval.algorithm.LenskitAlgorithmInstance;
import org.grouplens.lenskit.eval.algorithm.LenskitAlgorithmInstanceCommand;
import org.grouplens.lenskit.eval.config.EvalConfig;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.crossfold.CrossfoldCommand;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.TestUserMetric;
import org.grouplens.lenskit.util.table.Table;

import java.io.File;
import java.util.Properties;

public class SimpleEvalCommand extends AbstractCommand<Table>{

    private TrainTestEvalCommand result;

    /**
     * Configure any default behaviors for
     */
    protected void init(){
        result.setOutput(null);
    }
    /**
     * Constructs a SimpleConfigCommand with a name for the command and the {@code TrainTestEvalCommand}
     * @param commandName The name of the {@code SimpleConfigCommand}
     * @param trainName The name of the {@code TrainTestEvalCommand} being created.
     */
    public SimpleEvalCommand(String commandName, String trainName){
        super(commandName);
        result = new TrainTestEvalCommand(trainName);
        init();
    }

    /**
     * Constructs the command with a default name. Currently this is 'train-test-builder'.
     * @param trainName The name
     */
    public SimpleEvalCommand(String trainName){
        super("train-test-builder");
        result = new TrainTestEvalCommand(trainName);
        init();
    }

    /**
     * Constructs the command with a default name. Currently this is 'train-test-builder'.
     *
     * The command built has the name "train-test-eval"
     */
    public SimpleEvalCommand(){
        super("train-test-builder");
        result = new TrainTestEvalCommand("train-test-eval");
        init();
    }

    /**
     * Adds an algorithm to the {@code TrainTestEvalCommand} being built.
     *
     * If any exception is thrown while the command is called it is rethrown as a runtime error.
     * @param algo The algorithm added to the {@code TrainTestEvalCommand}
     * @return Itself to allow  chaining
     */
    public SimpleEvalCommand addAlgorithm(LenskitAlgorithmInstance algo){
        result.addAlgorithm(algo);
        return this;
    }

    /**
     * Adds a fully configured algorithm command to the {@code TrainTestEvalCommand} being built.
     *
     * @param algo The algorithm added to the {@code TrainTestEvalCommand}
     * @return Itself to allow  chaining
     */
    public SimpleEvalCommand addAlgorithm(LenskitAlgorithmInstanceCommand algo){
        try{
            result.addAlgorithm(algo.call());
        } catch(CommandException e){
            throw new RuntimeException(e);
        }
        return this;
    }


    /**
     * Calls the {@code CrossfoldCommand} and adds the resulting {@code TTDataSet}s to the {@code TrainTestEvalCommand}.
     *
     * Any exceptions that are thrown are wrapped as {@code RuntimeExceptions}.
     *
     * @param cross
     * @return Itself to allow for  method chaining.
     */
    public SimpleEvalCommand addDataset(CrossfoldCommand cross){
        try {
            for (TTDataSet data: cross.call()) {
                result.addDataset(data);
            }
        }
        catch (CommandException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Constructs a new {@code CrossfoldCommand} and configures it before adding the datasets
     * to the {@code TrainTestEvalCommand}.
     *
     * @param name The name of the crossfold
     * @param source The source for the crossfold
     * @param partitions The number of partitions
     * @param holdout The holdout fraction
     * @return Itself for chaining.
     */
    public SimpleEvalCommand addDataset(String name, DataSource source, int partitions, double holdout){
        CrossfoldCommand cross = new CrossfoldCommand(name)
                .setSource(source)
                .setPartitions(partitions)
                .setHoldoutFraction(holdout);
        cross.setConfig(getConfig());
        addDataset(cross);
        return this;
    }

    /**
     * Constructs a new {@code CrossfoldCommand} with the same name as its datasource
     * and configures it before adding the datasets * to the {@code TrainTestEvalCommand}.
     *
     * @param source The source for the crossfold
     * @param partitions The number of partitions
     * @param holdout The holdout fraction
     * @return Itself for chaining.
     */
    public SimpleEvalCommand addDataset(DataSource source, int partitions, double holdout){
        return addDataset(source.getName(), source, partitions, holdout);
    }
    /**
     * Constructs a new {@code CrossfoldCommand} and configures it before adding the datasets
     * to the {@code TrainTestEvalCommand}.
     *
     * It defaults the holdout to .2
     *
     * @param name The name of the crossfold
     * @param source The source for the crossfold
     * @param partitions The number of partitions
     * @return Itself for chaining.
     */
    public SimpleEvalCommand addDataset(String name, DataSource source, int partitions){
       return addDataset(name, source, partitions, .2);
    }

    /**
     * Constructs a new {@code CrossfoldCommand} and configures it before adding the datasets
     * to the {@code TrainTestEvalCommand}.
     *
     * It defaults the holdout to .2 and the name of the crossfold to the name of the data source.
     *
     * @param source The source for the crossfold
     * @param partitions The number of partitions
     * @return Itself for chaining.
     */
    public SimpleEvalCommand addDataset(DataSource source, int partitions){
        return addDataset(source.getName(), source, partitions, .2);
    }

    /**
     * Adds a single {@code TTDataSet} to the {@code TrainTestEvalCommand}.
     *
     * This acts a wrapper around {@code TrainTestEvalCommand.addDataset}
     * @param data The dataset to be added to the command.
     * @return Itself to allow for  method chaining.
     */
    public SimpleEvalCommand addDataset(TTDataSet data) {
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
    public SimpleEvalCommand addDataset(String name, DAOFactory train, DAOFactory test, PreferenceDomain dom){
        result.addDataset(new GenericTTDataSet(name, train, test, dom));
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
     * @param dom The {@code PreferenceDomain} to be supplied to the new {@code TTDataSet}
     * @return Itself for  method chaining.
     */
    public SimpleEvalCommand addDataset(DAOFactory train, DAOFactory test, PreferenceDomain dom){
        result.addDataset(new GenericTTDataSet("generic-data-source", train, test, dom));
        return this;
    }
    /**
     * Adds a completed metric to the {@code TrainTestEvalCommand}
     * @param metric The metric to be added.
     * @return Itself for  method chaining.
     */
    public SimpleEvalCommand addMetric(TestUserMetric metric) {
        result.addMetric(metric);
        return this;
    }

    /**
     * This provides a wrapper around {@code TrainTestEvalCommand.setOutput()}
     * @param file The file set as the output of the command
     * @return Itself for  method chaining
     */
    public SimpleEvalCommand setOutput(File file){
        result.setOutput(file);
        return this;
    }

    /**
     * This provides a wrapper around {@code TrainTestEvalCommand.setPredictOutput}
     * @param file The file set as the prediction output.
     * @return
     */
    public SimpleEvalCommand setPredictOutput(File file){
        result.setPredictOutput(file);
        return this;
    }


    /**
     * This provides a wrapper around {@code TrainTestEvalCommand.setUserOutput}
     * @param file The file set as the prediction user.
     * @return
     */
    public SimpleEvalCommand setUserOutput(File file){
        result.setUserOutput(file);
        return this;
    }

    /**
     * Creates a new file with the {@code name} and passes it to
     * {@code TrainTestEvalCommand.setOutput()}
     * @param path The path to the file to be created
     * @return Itself for method chaining
     */
    public SimpleEvalCommand setOutputPath(String path){
        result.setOutput(new File(path));
        return this;
    }
    /**
     * Creates a new file with the {@code name} and passes it to
     * {@code TrainTestEvalCommand.setPredictOutput()}
     * @param path The path to the file to be created
     * @return Itself for method chaining
     */
    public SimpleEvalCommand setPredictOutputPath(String path){
        result.setPredictOutput(new File(path));
        return this;
    }

    /**
     * Creates a new file with the {@code name} and passes it to
     * {@code TrainTestEvalCommand.setUserOutput()}
     * @param path The path to the file to be created
     * @return Itself for method chaining
     */
    public SimpleEvalCommand setUserOutputPath(String path){
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
    public TrainTestEvalCommand getRawCommand(){
        return result;
    }

    /**
     * If this is called more than once it will call of these commands again and most likely throw an exception.
     *
     * @return The table resulting from calling the command.
     */
    public Table call() throws CommandException{
        result.setConfig(getConfig());
        return result.call();
    }
}

