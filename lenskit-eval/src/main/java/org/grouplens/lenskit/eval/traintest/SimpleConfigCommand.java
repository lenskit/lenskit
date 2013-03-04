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

import java.util.ArrayList;
import java.util.Properties;

public class SimpleConfigCommand extends AbstractCommand<TrainTestEvalCommand>{

    private EvalConfig config = new EvalConfig(new Properties());
    private ArrayList<LenskitAlgorithmInstanceCommand> algoCommandList = new ArrayList<LenskitAlgorithmInstanceCommand>();
    private ArrayList<CrossfoldCommand> crossfoldList = new ArrayList<CrossfoldCommand>();
    private TrainTestEvalCommand result = new TrainTestEvalCommand();

    public SimpleConfigCommand(String name){
        super(name);
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
        crossfoldList.add(newCross);
        return newCross;
    }

    // Metrics
    public SimpleConfigCommand addMetric(TestUserMetric metric) {
        result.addMetric(metric);
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
