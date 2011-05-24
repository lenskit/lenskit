package org.grouplens.lenskit.eval.traintest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.predict.PredictionEvaluator;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scriptable class for building evaluation recipes.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class ScriptedRecipeBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ScriptedRecipeBuilder.class);
    private List<PredictionEvaluator> evaluators = new ArrayList<PredictionEvaluator>();
    private List<AlgorithmInstance> algorithms = new ArrayList<AlgorithmInstance>();
    private Scriptable scope;
    
    public ScriptedRecipeBuilder(Scriptable scope) {
        this.scope = scope; 
    }
    
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
    
    public List<AlgorithmInstance> getAlgorithms() {
        return algorithms;
    }
    
    EvaluationRecipe build(File file) {
        logger.info("Loaded {} algorithms", algorithms.size());
        return new EvaluationRecipe(algorithms, evaluators, file);
    }
}