package org.grouplens.lenskit.eval.traintest;

import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AbstractCommand;
import org.grouplens.lenskit.eval.CommandException;
import org.grouplens.lenskit.eval.algorithm.LenskitAlgorithmInstance;
import org.grouplens.lenskit.eval.algorithm.LenskitAlgorithmInstanceCommand;
import org.grouplens.lenskit.eval.config.EvalConfig;
import org.grouplens.lenskit.eval.data.crossfold.CrossfoldCommand;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.TestUserMetric;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

public class SimpleConfigCommand extends AbstractCommand<TrainTestEvalCommand>{

    private EvalConfig config = new EvalConfig(new Properties());
    private ArrayList<LenskitAlgorithmInstanceCommand> algoCommandList = new ArrayList<LenskitAlgorithmInstanceCommand>();
    private ArrayList<CrossfoldCommand> crossfoldList = new ArrayList<CrossfoldCommand>();
    private TrainTestEvalCommand result;

    /**
     * Constructs a SimpleConfigCommand with a name for the command and the {@code TrainTestEvalCommand}
     * @param commandName The name of the {@code SimpleConfigCommand}
     * @param trainName The name of the {@code TrainTestEvalCommand} being created.
     */
    public SimpleConfigCommand(String commandName, String trainName){
        super(commandName);
        result = new TrainTestEvalCommand(trainName);
    }

    /**
     * @param commandName The name of the command being constructed
     * @param trainName The name of the {@code TrainTestEvalCommand}
     * @param config The EvalConfig supplied to the {@code TrainTestEvalCommand}
     */
    public SimpleConfigCommand(String commandName, String trainName, EvalConfig config){
        super(commandName);
        this.config = config;
        result = new TrainTestEvalCommand(trainName);
    }

    /**
     * Constructs the command with a default name. Currently this is 'train-test-builder'.
     * @param trainName The name
     */
    public SimpleConfigCommand(String trainName){
        super("train-test-builder");
        result = new TrainTestEvalCommand(trainName);
        result.setOutput(null);
    }

    /**
     * Adds a fully configured algorithm command to the {@code TrainTestEvalCommand} being built.
     *
     * If any exception is thrown while the command is called it is rethrown as a runtime error.
     * @param algo The algorithm added to the {@code TrainTestEvalCommand}
     * @return Itself to allow  chaining
     */
    public SimpleConfigCommand  addCompleteAlgorithm(LenskitAlgorithmInstanceCommand algo){
        try{
            result.addAlgorithm(algo.call());
        }
        catch(CommandException e){
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Adds a {@code LenskitAlgorithmInstance} to the {@code TrainTestEvalCommand} being built.
     * This acts as a simple wrapper around TrainTestEval.addAlgorithm
     *
     * @param algo The AlgorithmInstance supplied to the {@code TrainTestEvalCommand}
     * @return
     */
    public SimpleConfigCommand addCompleteAlgorithm(LenskitAlgorithmInstance algo){
        result.addAlgorithm(algo);
        return this;
    }

    /**
     * Creates a {@code LenskitAlgorithmInstanceCommand} with {@code algo} as a name.
     *
     * It is {@code call}'ed added to the {@code TrainTestEvalCommand} only when the {@code call()} method is called.
     * @param algo The name of the algorithm to be created
     * @return The {@code LenskitRecommenderEngineFactory} for the newly created algorithm to allow customization
     */
    public LenskitRecommenderEngineFactory addAlgorithm(String algo){
        LenskitAlgorithmInstanceCommand command = new LenskitAlgorithmInstanceCommand(algo);
        algoCommandList.add(command);
        return command.getFactory();
    }

    /**
     * This is identical to {@code addAlgorithm(String algo)} except no name is provided to the {@code LenskitAlgorithmInstanceCommand}
     * @return The factory of the new command for customization.
     */
    public LenskitRecommenderEngineFactory addAlgorithm(){
        LenskitAlgorithmInstanceCommand command = new LenskitAlgorithmInstanceCommand();
        algoCommandList.add(command);
        return command.getFactory();
    }

    /**
     * Adds the prebuilt {@code LenskitAlgorithmInstanceCommand} to the list of {@code AlgorithmInstance} commands.
     *
     * The command will be called and added to the {@code TrainTestEvalCommand} when the {@code SimpleConfigCommand} is called.
     * @param algo The constructed {@code LenskitAlgorithmInstanceCommand}
     * @return The factory of the command to allow for further customization.
     */
    public LenskitRecommenderEngineFactory addAlgorithm(LenskitAlgorithmInstanceCommand algo){
        algoCommandList.add(algo);
        return algo.getFactory();
    }

    /**
     * Calls the {@code CrossfoldCommand} and adds the resulting {@code TTDataSet}s to the {@code TrainTestEvalCommand}.
     *
     * Any exceptions that are thrown are wrapped as {@code RuntimeExceptions}.
     *
     * @param cross
     * @return Itself to allow for  method chaining.
     */
    public SimpleConfigCommand addCompleteDataset(CrossfoldCommand cross){
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
     * Adds a single {@code TTDataSet} to the {@code TrainTestEvalCommand}.
     *
     * This acts a wrapper around {@code TrainTestEvalCommand.addDataset}
     * @param data The dataset to be added to the command.
     * @return Itself to allow for  method chaining.
     */
    public SimpleConfigCommand addCompleteDataset(TTDataSet data) {
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
    public SimpleConfigCommand addCompleteDataset(String name, DAOFactory train, DAOFactory test, PreferenceDomain dom){
        result.addDataset(new GenericTTDataSet(name, train, test, dom));
        return this;
    }

    /**
     * Adds a constructed but unconfigured {@code CrossfoldCommand} to a list of commands that are added to the {@code TrainTestEvalCommand}
     * when the {@code call()} method is called.
     * @param cross
     * @return {@code cross} to allow for further configuration.
     */
    public CrossfoldCommand addCrossfold(CrossfoldCommand cross) {
        crossfoldList.add(cross);
        return cross;
    }

    /**
     * Similar to {addCrossfold(CrossfoldCommand)} except this method constructs a new command with the name
     * {@code name}
     * @param name The name supplied to the {@code CrossfoldCommand}
     * @return The newly constructed for configuration.
     */
    public CrossfoldCommand addCrossfold(String name) {
        CrossfoldCommand newCross = new CrossfoldCommand(name);
        newCross.setConfig(config);
        crossfoldList.add(newCross);
        return newCross;
    }

    /**
     * Adds a completed metric to the {@code TrainTestEvalCommand}
     * @param metric The metric to be added.
     * @return Itself for  method chaining.
     */
    public SimpleConfigCommand addMetric(TestUserMetric metric) {
        result.addMetric(metric);
        return this;
    }

    /**
     * This provides a wrapper around {@code TrainTestEvalCommand.setOutput()}
     * @param file The file set as the output of the command
     * @return Itself for  method chaining
     */
    public SimpleConfigCommand setOutput(File file){
        result.setOutput(file);
        return this;
    }

    /**
     * This provides a wrapper around {@code TrainTestEvalCommand.setPredictOutput}
     * @param file The file set as the prediction output.
     * @return
     */
    public SimpleConfigCommand setPredictOutput(File file){
        result.setPredictOutput(file);
        return this;
    }


    /**
     * This provides a wrapper around {@code TrainTestEvalCommand.setUserOutput}
     * @param file The file set as the prediction user.
     * @return
     */
    public SimpleConfigCommand setUserOutput(File file){
        result.setUserOutput(file);
        return this;
    }

    /**
     * Creates a new file with the {@code name} and passes it to
     * {@code TrainTestEvalCommand.setOutput()}
     * @param path The path to the file to be created
     * @return Itself for method chaining
     */
    public SimpleConfigCommand setOutputPath(String path){
        result.setOutput(new File(path));
        return this;
    }
    /**
     * Creates a new file with the {@code name} and passes it to
     * {@code TrainTestEvalCommand.setPredictOutput()}
     * @param path The path to the file to be created
     * @return Itself for method chaining
     */
    public SimpleConfigCommand setPredictOutputPath(String path){
        result.setPredictOutput(new File(path));
        return this;
    }

    /**
     * Creates a new file with the {@code name} and passes it to
     * {@code TrainTestEvalCommand.setUserOutput()}
     * @param path The path to the file to be created
     * @return Itself for method chaining
     */
    public SimpleConfigCommand setUserOutputPath(String path){
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
     * Calls all the stored {@code LenskitAlgorithmInstanceCommand} and {@code CrossfoldCommand} and adds them to the
     * command.
     *
     * If this is called more than once it will call of these commands again and most likely throw an exception.
     *
     * @return The fully configured command.
     */
    public TrainTestEvalCommand call() throws CommandException{
        for (LenskitAlgorithmInstanceCommand algo : algoCommandList) {
            result.addAlgorithm(algo.call());
        }
        for (CrossfoldCommand cross : crossfoldList) {
            for(TTDataSet data : cross.call()){
                result.addDataset(data);
            }
        }
        result.setConfig(config);
        return result;
    }
}
