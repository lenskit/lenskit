package org.lenskit.solver;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ExpectationMaximization extends AbstractOptimizationMethod {
    private int maxIter;
    private double tol;
    private OptimizationMethod method;
    
    public ExpectationMaximization() {
        maxIter = 50;
        tol = 1.0;
        method = new StochasticGradientDescent(3, 0.0, 0.01, 10);
    }

    public double minimize(LearningModel learningModel, LearningData learningData, LearningData validData) {
        //check the type of learningModel
        LatentLearningModel model = (LatentLearningModel)learningModel;
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        double objVal = 0;
        while (termCrit.keepIterate()) {
            objVal = 0;
            learningData.startNewIteration();
            LearningInstance ins;
            while ((ins = learningData.getLearningInstance()) != null) {
                objVal += model.expectation(ins);
            }
            termCrit.addIteration(objVal);
            LearningModel subModel = model.maximization();
            if (subModel != null) {
                method.minimize(subModel, learningData, validData);
            }
        }
        return objVal;
    }
}
