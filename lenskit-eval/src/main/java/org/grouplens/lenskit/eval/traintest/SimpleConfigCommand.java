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

    private EvalConfig config                                          = new EvalConfig(new Properties());
    private ArrayList<LenskitAlgorithmInstanceCommand> algoCommandList = new ArrayList<LenskitAlgorithmInstanceCommand>();
    private ArrayList<CrossfoldCommand> crossfoldList                  = new ArrayList<CrossfoldCommand>();
    private TrainTestEvalCommand result;

    public SimpleConfigCommand(String commandName, String trainName){
        super(commandName);
        result = new TrainTestEvalCommand(trainName);
    }
    public SimpleConfigCommand(String trainName){
        super("train-test-builder");
        result = new TrainTestEvalCommand(trainName);
        result.setOutput(null);
    }

    // Eval config
    public SimpleConfigCommand setConfig(EvalConfig config){
        this.config = config;
        return this;
    }
    public SimpleConfigCommand setConfig(Properties props){
        this.config = new EvalConfig(props);
        return this;
    }

    // Algorithms
    public SimpleConfigCommand  addCompleteAlgorithm(LenskitAlgorithmInstanceCommand algo){
        try{
            result.addAlgorithm(algo.call());
        }
        catch(CommandException e){
            throw new RuntimeException(e);
        }
        return this;
    }
    public SimpleConfigCommand addCompleteAlgorithm(LenskitAlgorithmInstance algo){
        result.addAlgorithm(algo);
        return this;
    }
    public LenskitRecommenderEngineFactory addAlgorithm(String algo){
        LenskitAlgorithmInstanceCommand command = new LenskitAlgorithmInstanceCommand(algo);
        algoCommandList.add(command);
        return command.getFactory();
    }
    public LenskitRecommenderEngineFactory addAlgorithm(){
        LenskitAlgorithmInstanceCommand command = new LenskitAlgorithmInstanceCommand();
        algoCommandList.add(command);
        return command.getFactory();
    }
    public LenskitRecommenderEngineFactory addAlgorithm(LenskitAlgorithmInstanceCommand algo){
        algoCommandList.add(algo);
        return algo.getFactory();
    }
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
    // Datasets
    public SimpleConfigCommand addCompleteDataset(TTDataSet data) {
        result.addDataset(data);
        return this;
    }
    public SimpleConfigCommand addCompleteDataset(String name, DAOFactory train, DAOFactory test, PreferenceDomain dom){
        result.addDataset(new GenericTTDataSet(name, train, test, dom));
        return this;
    }
    public CrossfoldCommand addCrossfold(CrossfoldCommand cross) {
        crossfoldList.add(cross);
        return cross;
    }
    public CrossfoldCommand addCrossfold(String name) {
        CrossfoldCommand newCross = new CrossfoldCommand(name);
        newCross.setConfig(config);
        crossfoldList.add(newCross);
        return newCross;
    }

    // Metrics
    public SimpleConfigCommand addMetric(TestUserMetric metric) {
        result.addMetric(metric);
        return this;
    }

    //Output
    public SimpleConfigCommand setOutput(File file){
        result.setOutput(file);
        return this;
    }

    public SimpleConfigCommand setPredictOutput(File file){
        result.setPredictOutput(file);
        return this;
    }


    public SimpleConfigCommand setUserOutput(File file){
        result.setUserOutput(file);
        return this;
    }

    public SimpleConfigCommand setPredictOutput(String name){
        result.setPredictOutput(new File(name));
        return this;
    }

    public SimpleConfigCommand setUserOutput(String name){
        result.setUserOutput(new File(name));
        return this;
    }

    //Raw Access
    public TrainTestEvalCommand getRawCommand(){
        return result;
    }

    public TrainTestEvalCommand call(){
        try {
            for (LenskitAlgorithmInstanceCommand algo : algoCommandList) {
                result.addAlgorithm(algo.call());
            }
            for (CrossfoldCommand cross : crossfoldList) {
                for(TTDataSet data : cross.call()){
                    result.addDataset(data);
                }
            }
        }
        catch (CommandException e) {
            throw new RuntimeException(e);
        }
        result.setConfig(config);
        return result;
    }
}
