package org.lenskit.solver;

abstract public class AbstractOnlineOptimizationMethod extends AbstractOptimizationMethod implements OnlineOptimizationMethod {
    protected double tol;
    protected int maxIter;

    public double minimize(LearningModel learningModel, LearningData learningData, LearningData validData) {
        ObjectiveTerminationCriterion learnCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        ObjectiveTerminationCriterion validCrit = null;
        if (validData != null) {
            validCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        }
        double learnObjVal = 0.0;
        while (learnCrit.keepIterate()) {
            if (validCrit != null && !(validCrit.keepIterate())) {
                break;
            }
            learningData.startNewIteration();
            learnObjVal = update(learningModel, learningData);
            learnCrit.addIteration(AbstractOnlineOptimizationMethod.class.toString()
                    + " -- Learning", learnObjVal);
            if (validData != null) {
                double validObjVal = evaluate(learningModel, validData);
                validCrit.addIteration(AbstractOnlineOptimizationMethod.class.toString()
                        + " -- Validating", validObjVal);
            }
        }
        return learnObjVal;
    }

}
