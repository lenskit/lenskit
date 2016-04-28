package org.lenskit.solver;

abstract public class AbstractOnlineOptimizationMethod implements OnlineOptimizationMethod {
    protected double tol;
    protected int maxIter;

    public double minimize(LearningModel learningModel, LearningData learningData) {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        double objVal = 0.0;
        while (termCrit.keepIterate()) {
            learningData.startNewIteration();
            objVal = update(learningModel, learningData);
            termCrit.addIteration(AbstractOnlineOptimizationMethod.class.toString(), objVal);
        }
        return objVal;
    }

}
